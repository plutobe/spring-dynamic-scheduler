package com.plutobe.open.scheduler.core;

import com.plutobe.open.scheduler.bean.RunningTaskBean;
import com.plutobe.open.scheduler.entity.TaskEntity;
import com.plutobe.open.scheduler.entity.TaskLogEntity;
import com.plutobe.open.scheduler.enu.TaskStatusEnum;
import com.plutobe.open.scheduler.repository.TaskLogRepository;
import com.plutobe.open.scheduler.repository.TaskRepository;
import com.plutobe.open.scheduler.util.SpringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.BeansException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * 定时任务容器
 *
 * @author plutobe@outlook.com
 * @date 2020-04-13
 */
@Slf4j
@Component
public class TaskContainer {

    @Resource
    private TaskRepository taskRepository;

    @Resource
    private TaskLogRepository taskLogRepository;

    @Resource
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;

    /**
     * 定时任务列表
     */
    public static final Map<Long, RunningTaskBean> RUNNING_TASK_MAP = new ConcurrentHashMap<>();

    /**
     * 添加任务
     *
     * @param taskEntity
     * @return
     */
    public void addTask(TaskEntity taskEntity) {
        if (RUNNING_TASK_MAP.containsKey(taskEntity.getId())) {
            RunningTaskBean taskBean = RUNNING_TASK_MAP.get(taskEntity.getId());
            if (taskEntity.getCron().equals(taskBean.getCron())) {
                return;
            }
        }
        ScheduledFuture<?> future = threadPoolTaskScheduler.schedule(() -> {
            try {
                this.invoke(taskEntity);
            } catch (BeansException | NoSuchMethodException e) {
                log.error("定时任务方法调用异常", e);
            }
        }, new CronTrigger(taskEntity.getCron()));
        RunningTaskBean taskBean = new RunningTaskBean();
        taskBean.setCron(taskEntity.getCron());
        taskBean.setFuture(future);
        RUNNING_TASK_MAP.put(taskEntity.getId(), taskBean);
        log.info("定时任务启动成功 - taskId={}", taskEntity.getId());
    }

    /**
     * 删除任务
     *
     * @param taskId
     */
    public void removeTask(Long taskId) {
        if (!RUNNING_TASK_MAP.containsKey(taskId)) {
            return;
        }
        RunningTaskBean taskBean = RUNNING_TASK_MAP.get(taskId);
        ScheduledFuture<?> future = taskBean.getFuture();
        if (future != null) {
            future.cancel(true);
        }
        RUNNING_TASK_MAP.remove(taskId);
        log.info("定时任务停止成功 - taskId={}", taskId);
    }

    /**
     * 调用指定任务方法
     *
     * @param taskEntity
     * @throws BeansException
     * @throws NoSuchMethodException
     */
    private void invoke(TaskEntity taskEntity) throws BeansException, NoSuchMethodException {
        // 任务进入时间
        Date joinTime = new Date();
        taskEntity = taskRepository.getOne(taskEntity.getId());
        if (checkTaskStatusIsShutoff(taskEntity)) {
            // 停止状态
            return;
        }
        if (hasSyncCron(taskEntity)) {
            // 需要同步cron 定时任务已做了修改
            return;
        }
        if (!taskEntity.getIdempotent()) {
            // 非幂等操作
            if (TaskStatusEnum.RUNNING.equals(taskEntity.getStatus()) || hasExecuted(taskEntity, joinTime)) {
                log.info("已有其他节点正在执行该任务 或该任务已被其他节点执行过");
                return;
            }
        }
        // 锁定任务状态
        lockStatus(taskEntity.getId(), TaskStatusEnum.RUNNING);
        // 计时器计算任务执行消耗时长
        StopWatch stopWatch = new StopWatch();
        String errorMsg = null;
        stopWatch.start();
        try {
            // 开始执行任务
            log.info("定时任务执行开始 taskId={}", taskEntity.getId());
            Object bean = SpringUtils.getBean(taskEntity.getBeanName());
            Method method = bean.getClass().getDeclaredMethod(taskEntity.getMethodName());
            method.invoke(bean);
            log.info("定时任务执行成功 taskId={}", taskEntity.getId());
        } catch (Throwable throwable) {
            log.error("定时任务执行异常", throwable);
            errorMsg = ExceptionUtils.getStackTrace(throwable);
        }
        stopWatch.stop();
        long totalTimeMillis = stopWatch.getTotalTimeMillis();

        // 解锁任务状态
        lockStatus(taskEntity.getId(), TaskStatusEnum.ACTIVE);

        // 定时任务执行完毕后 将任务状态恢复正常并记录相关数据
        saveTaskData(taskEntity.getId(), totalTimeMillis, joinTime, errorMsg);
    }

    /**
     * 检查任务状态是否为停止状态
     * <p>
     * 若数据库状态为停止状态但在定时任务线程池里依然在执行该任务 则需要移除线程池里的该任务
     * 该原因是因为多节点拥有多任务线程池 所以会出现该现象
     *
     * @param taskEntity
     * @return
     */
    private boolean checkTaskStatusIsShutoff(TaskEntity taskEntity) {
        boolean isShutoff = TaskStatusEnum.SHUTOFF.equals(taskEntity.getStatus());
        if (isShutoff) {
            this.removeTask(taskEntity.getId());
        }
        return isShutoff;
    }

    /**
     * 同步cron
     *
     * @param taskEntity
     */
    private boolean hasSyncCron(TaskEntity taskEntity) throws NoSuchMethodException {
        String cron = TaskContainer.RUNNING_TASK_MAP.get(taskEntity.getId()).getCron();
        if (!cron.equals(taskEntity.getCron())) {
            this.removeTask(taskEntity.getId());
            this.addTask(taskEntity);
            return true;
        }
        return false;
    }

    /**
     * 锁定状态
     *
     * @param taskId
     */
    private void lockStatus(Long taskId, TaskStatusEnum status) {
        TaskEntity taskEntityUpdateStatusRunning = new TaskEntity();
        taskEntityUpdateStatusRunning.setId(taskId);
        taskEntityUpdateStatusRunning.setStatus(status);
        taskRepository.save(taskEntityUpdateStatusRunning);
    }

    /**
     * 当前任务是否已执行
     *
     * @param taskEntity
     * @param aspectJoinTime
     * @return
     */
    private boolean hasExecuted(TaskEntity taskEntity, Date aspectJoinTime) {
        Date lastExecutionTime = taskEntity.getLastExecutionTime();
        if (lastExecutionTime == null) {
            return false;
        }
        boolean sameDay = DateUtils.isSameDay(lastExecutionTime, aspectJoinTime);
        if (!sameDay) {
            return false;
        }
        // cron最小精度到秒 故取时间最小单位到秒判定即可
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(lastExecutionTime);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        Calendar calendarNow = Calendar.getInstance();
        calendarNow.setTime(aspectJoinTime);
        int hourNow = calendarNow.get(Calendar.HOUR_OF_DAY);
        int minuteNow = calendarNow.get(Calendar.MINUTE);
        int secondNow = calendarNow.get(Calendar.SECOND);
        return hour == hourNow && minute == minuteNow && second == secondNow;
    }

    /**
     * 保存定时任务相关数据
     *
     * @param taskId
     * @param totalTimeMillis
     * @param joinTime
     * @param errorMsg
     * @return
     */
    private void saveTaskData(Long taskId, long totalTimeMillis, Date joinTime, String errorMsg) {
        TaskEntity taskEntity = taskRepository.getOne(taskId);
        TaskEntity taskEntityUpdate = new TaskEntity();
        taskEntityUpdate.setId(taskEntity.getId());
        taskEntityUpdate.setLastExecutionTime(joinTime);
        taskEntityUpdate.setDuration(totalTimeMillis);
        taskEntityUpdate.setExecutionTimes(taskEntity.getExecutionTimes() + 1);
        taskRepository.save(taskEntityUpdate);
        // 保存定时任务执行日志
        saveTaskLog(errorMsg, totalTimeMillis, taskEntity);
    }

    /**
     * 保存定时任务执行日志
     *
     * @param errorMsg
     * @param totalTimeMillis
     * @param taskEntity
     */
    private void saveTaskLog(String errorMsg, long totalTimeMillis, TaskEntity taskEntity) {
        TaskLogEntity taskLogEntity = new TaskLogEntity();
        Date endTime = new Date();
        taskLogEntity.setTaskId(taskEntity.getId());
        taskLogEntity.setStartTime(new Date(endTime.getTime() - totalTimeMillis));
        taskLogEntity.setEndTime(endTime);
        taskLogEntity.setDuration(totalTimeMillis);
        taskLogEntity.setSuccess(errorMsg == null);
        taskLogEntity.setServerName(System.getenv().get("COMPUTERNAME"));
        taskLogEntity.setRemark(errorMsg);
        taskLogRepository.save(taskLogEntity);
    }

}

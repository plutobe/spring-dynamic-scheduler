package com.plutobe.open.scheduler.core;

import com.plutobe.open.scheduler.entity.TaskEntity;
import com.plutobe.open.scheduler.enu.TaskStatusEnum;
import com.plutobe.open.scheduler.repository.TaskRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * 定时任务加载器
 *
 * @author plutobe@outlook.com
 * @date 2020-04-13
 */
@Slf4j
@Component
public class TaskLoader implements CommandLineRunner {

    @Resource
    private TaskContainer taskContainer;

    @Resource
    private TaskRepository taskRepository;

    @Override
    public void run(String... args) throws Exception {
        // 系统启动后载入数据库中启动状态的定时任务
        List<TaskEntity> taskEntityList = taskRepository.findByStatus(TaskStatusEnum.ACTIVE);
        if (!CollectionUtils.isEmpty(taskEntityList)) {
            for (TaskEntity taskEntity : taskEntityList) {
                try {
                    taskContainer.addTask(taskEntity);
                } catch (Exception e) {
                    log.error("定时任务 - {} 启动失败", taskEntity.getTaskName(), e);
                    continue;
                }
                log.info("定时任务 - {} 启动成功", taskEntity.getTaskName());
            }
            log.info("已加载全部定时任务到任务池");
        }
    }

}

package com.plutobe.open.scheduler.service.impl;

import com.plutobe.open.scheduler.core.TaskContainer;
import com.plutobe.open.scheduler.entity.TaskEntity;
import com.plutobe.open.scheduler.enu.TaskStatusEnum;
import com.plutobe.open.scheduler.repository.TaskRepository;
import com.plutobe.open.scheduler.service.TaskService;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 定时任务service实现类
 *
 * @author plutobe@outlook.com
 * @date 2020-04-13
 */
@Service
public class TaskServiceImpl implements TaskService {

    @Resource
    private TaskRepository taskRepository;

    @Resource
    private TaskContainer taskContainer;

    /**
     * 根据id查找
     *
     * @param id
     * @return
     */
    @Override
    public TaskEntity findById(Long id) {
        return taskRepository.findById(id).orElse(null);
    }

    /**
     * 查询所有
     *
     * @return
     */
    @Override
    public List<TaskEntity> findAll() {
        return taskRepository.findAll();
    }

    /**
     * 查询分页列表
     *
     * @param taskEntity
     * @param pageable
     * @return
     */
    @Override
    public Page<TaskEntity> findPageByExample(TaskEntity taskEntity, Pageable pageable) {
        return taskRepository.findAll(Example.of(taskEntity), pageable);
    }

    /**
     * 添加定时任务
     *
     * @param taskEntity
     */
    @Override
    public void addTask(TaskEntity taskEntity) {
        taskRepository.save(taskEntity);
        if (TaskStatusEnum.ACTIVE.equals(taskEntity.getStatus())) {
            taskContainer.addTask(taskEntity);
        }
    }

    /**
     * 更新定时任务
     *
     * @param taskEntity
     */
    @Override
    public void updateTask(TaskEntity taskEntity) {
        taskRepository.save(taskEntity);
        if (TaskStatusEnum.ACTIVE.equals(taskEntity.getStatus())) {
            stopTask(taskEntity.getId());
            startupTask(taskEntity.getId());
        }
        if (TaskStatusEnum.SHUTOFF.equals(taskEntity.getStatus())) {
            stopTask(taskEntity.getId());
        }
    }

    /**
     * 停止定时任务
     *
     * @param id
     */
    @Override
    public void stopTask(Long id) {
        TaskEntity taskEntity = findById(id);
        if (taskEntity != null) {
            taskContainer.removeTask(taskEntity.getId());
            taskEntity.setStatus(TaskStatusEnum.SHUTOFF);
            taskRepository.save(taskEntity);
        }
    }

    /**
     * 启动定时任务
     *
     * @param id
     */
    @Override
    public void startupTask(Long id) {
        TaskEntity taskEntity = findById(id);
        if (taskEntity != null) {
            taskContainer.addTask(taskEntity);
            taskEntity.setStatus(TaskStatusEnum.ACTIVE);
        }
    }

    /**
     * 删除定时任务
     *
     * @param id
     */
    @Override
    public void deleteTask(Long id) {
        stopTask(id);
        taskRepository.deleteById(id);
    }

}

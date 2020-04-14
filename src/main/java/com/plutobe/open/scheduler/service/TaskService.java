package com.plutobe.open.scheduler.service;

import com.plutobe.open.scheduler.entity.TaskEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 定时任务service
 *
 * @author plutobe@outlook.com
 * @date 2020-04-13
 */
public interface TaskService {

    /**
     * 根据id查找
     *
     * @param id
     * @return
     */
    TaskEntity findById(Long id);

    /**
     * 查询所有
     *
     * @return
     */
    List<TaskEntity> findAll();

    /**
     * 查询分页
     *
     * @param taskEntity
     * @param pageable
     * @return
     */
    Page<TaskEntity> findPageByExample(TaskEntity taskEntity, Pageable pageable);

    /**
     * 添加定时任务
     *
     * @param taskEntity
     */
    void addTask(TaskEntity taskEntity);

    /**
     * 更新定时任务
     *
     * @param taskEntity
     */
    void updateTask(TaskEntity taskEntity);

    /**
     * 停止定时任务
     *
     * @param id
     */
    void stopTask(Long id);

    /**
     * 启动定时任务
     *
     * @param id
     */
    void startupTask(Long id);

    /**
     * 删除定时任务
     *
     * @param id
     */
    void deleteTask(Long id);

}

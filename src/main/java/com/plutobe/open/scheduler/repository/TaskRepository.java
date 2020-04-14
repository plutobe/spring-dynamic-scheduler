package com.plutobe.open.scheduler.repository;

import com.plutobe.open.scheduler.entity.TaskEntity;
import com.plutobe.open.scheduler.enu.TaskStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 定时任务仓库
 *
 * @author plutobe@outlook.com
 * @date 2020-04-13
 */
@Repository
public interface TaskRepository extends JpaRepository<TaskEntity, Long> {

    /**
     * 根据任务状态查询
     *
     * @param status
     * @return
     */
    List<TaskEntity> findByStatus(TaskStatusEnum status);

}

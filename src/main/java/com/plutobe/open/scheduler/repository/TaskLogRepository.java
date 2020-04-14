package com.plutobe.open.scheduler.repository;

import com.plutobe.open.scheduler.entity.TaskLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 定时任务日志仓库
 *
 * @author plutobe@outlook.com
 * @date 2020-04-13
 */
@Repository
public interface TaskLogRepository extends JpaRepository<TaskLogEntity, Long> {

    /**
     * 根据taskId查询
     *
     * @param taskId
     * @return
     */
    List<TaskLogEntity> findByTaskId(Long taskId);

}

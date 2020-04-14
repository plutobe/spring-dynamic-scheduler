package com.plutobe.open.scheduler.service;

import com.plutobe.open.scheduler.entity.TaskLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 定时任务日志service
 *
 * @author plutobe@outlook.com
 * @date 2020-04-13
 */
public interface TaskLogService {

    /**
     * 根据taskId查找
     *
     * @param taskId
     * @return
     */
    List<TaskLogEntity> findListByTaskId(Long taskId);

    /**
     * 根据taskId查询分页
     *
     * @param taskId
     * @param pageable
     * @return
     */
    Page<TaskLogEntity> findPageByTaskId(Long taskId, Pageable pageable);

}

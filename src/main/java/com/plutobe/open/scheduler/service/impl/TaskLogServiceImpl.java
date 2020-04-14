package com.plutobe.open.scheduler.service.impl;

import com.plutobe.open.scheduler.entity.TaskLogEntity;
import com.plutobe.open.scheduler.repository.TaskLogRepository;
import com.plutobe.open.scheduler.service.TaskLogService;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 定时任务日志service实现类
 *
 * @author plutobe@outlook.com
 * @date 2020-04-13
 */
@Service
public class TaskLogServiceImpl implements TaskLogService {

    @Resource
    private TaskLogRepository taskLogRepository;

    /**
     * 根据taskId查找
     *
     * @param taskId
     * @return
     */
    @Override
    public List<TaskLogEntity> findListByTaskId(Long taskId) {
        return taskLogRepository.findByTaskId(taskId);
    }

    /**
     * 根据taskId查询分页
     *
     * @param taskId
     * @param pageable
     * @return
     */
    @Override
    public Page<TaskLogEntity> findPageByTaskId(Long taskId, Pageable pageable) {
        TaskLogEntity taskLogEntity = new TaskLogEntity();
        taskLogEntity.setTaskId(taskId);
        return taskLogRepository.findAll(Example.of(taskLogEntity), pageable);
    }

}

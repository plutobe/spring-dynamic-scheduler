package com.plutobe.open.scheduler.entity;

import com.plutobe.open.scheduler.base.BaseEntity;
import com.plutobe.open.scheduler.enu.TaskStatusEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.Date;

/**
 * 定时任务实体
 *
 * @author plutobe@outlook.com
 * @date 2020-04-13
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "sds_task")
public class TaskEntity extends BaseEntity {

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * cron表达式
     */
    private String cron;

    /**
     * 任务bean名称
     */
    private String beanName;

    /**
     * 任务method名称
     */
    private String methodName;

    /**
     * 是否幂等
     * 若多节点定时任务执行非幂等性操作的任务时 请务必设该参数为false
     * 执行幂等性操作的任务设为true/false均可
     */
    private Boolean idempotent;

    /**
     * 任务状态
     */
    @Enumerated(EnumType.STRING)
    private TaskStatusEnum status;

    /**
     * 上次执行时间
     */

    @Temporal(TemporalType.TIMESTAMP)
    private Date lastExecutionTime;

    /**
     * 执行持续时间
     */
    private Long duration;

    /**
     * 总执行次数
     */
    private Long executionTimes;

    /**
     * 备注
     */
    private String remark;

}

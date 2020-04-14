package com.plutobe.open.scheduler.entity;

import com.plutobe.open.scheduler.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

/**
 * 定时任务日志实体
 *
 * @author plutobe@outlook.com
 * @date 2020-04-13
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "sds_task_log")
public class TaskLogEntity extends BaseEntity {

    /**
     * 任务id
     */
    private Long taskId;

    /**
     * 任务开始时间
     */
    @Temporal(TemporalType.TIMESTAMP)
    private Date startTime;

    /**
     * 任务结束时间
     */
    @Temporal(TemporalType.TIMESTAMP)
    private Date endTime;

    /**
     * 耗时
     */
    private Long duration;

    /**
     * 任务是否成功
     */
    private Boolean success;

    /**
     * 服务器名称
     */
    private String serverName;

    /**
     * 备注
     */
    private String remark;

}

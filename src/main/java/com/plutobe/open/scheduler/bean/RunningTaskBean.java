package com.plutobe.open.scheduler.bean;

import lombok.Data;

import java.util.concurrent.ScheduledFuture;

/**
 * 执行中的定时任务bean
 *
 * @author plutobe@outlook.com
 * @date 2020-04-13
 */
@Data
public class RunningTaskBean {

    private String cron;

    private ScheduledFuture<?> future;

}

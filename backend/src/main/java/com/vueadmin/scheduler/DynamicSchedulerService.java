package com.vueadmin.scheduler;

import com.vueadmin.entity.system.ScheduleTask;
import com.vueadmin.service.system.ScheduleTaskService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * 动态任务调度服务
 */
@Slf4j
@Service
public class DynamicSchedulerService {

    private Scheduler scheduler;
    private final ScheduleTaskService scheduleTaskService;

    public DynamicSchedulerService(@Lazy ScheduleTaskService scheduleTaskService) {
        this.scheduleTaskService = scheduleTaskService;
    }

    @PostConstruct
    public void init() throws SchedulerException {
        SchedulerFactory schedulerFactory = new StdSchedulerFactory();
        scheduler = schedulerFactory.getScheduler();
        scheduler.start();
        log.info("Quartz调度器已启动");
    }

    /**
     * 注册定时任务
     */
    public void scheduleTask(ScheduleTask task) throws SchedulerException {
        String jobName = task.getTaskCode();
        String jobGroup = "MDM_TASKS";

        // 创建JobDetail
        JobDetail jobDetail = JobBuilder.newJob(DynamicJob.class)
                .withIdentity(jobName, jobGroup)
                .usingJobData("taskId", task.getId())
                .usingJobData("taskCode", task.getTaskCode())
                .usingJobData("taskClass", task.getTaskClass())
                .usingJobData("taskParams", task.getTaskParams())
                .storeDurably()
                .build();

        // 创建Trigger
        CronTrigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(jobName + "_TRIGGER", jobGroup)
                .withSchedule(CronScheduleBuilder.cronSchedule(task.getCronExpression()))
                .build();

        // 调度任务
        scheduler.scheduleJob(jobDetail, trigger);

        log.info("定时任务已注册: {} - {}", task.getTaskCode(), task.getCronExpression());
    }

    /**
     * 取消定时任务
     */
    public void unscheduleTask(String taskCode) {
        try {
            String jobName = taskCode;
            String jobGroup = "MDM_TASKS";

            JobKey jobKey = new JobKey(jobName, jobGroup);
            if (scheduler.checkExists(jobKey)) {
                scheduler.deleteJob(jobKey);
                log.info("定时任务已取消: {}", taskCode);
            }
        } catch (SchedulerException e) {
            log.error("取消定时任务失败: {}", taskCode, e);
        }
    }

    /**
     * 暂停定时任务
     */
    public void pauseTask(String taskCode) {
        try {
            JobKey jobKey = new JobKey(taskCode, "MDM_TASKS");
            if (scheduler.checkExists(jobKey)) {
                scheduler.pauseJob(jobKey);
                log.info("定时任务已暂停: {}", taskCode);
            }
        } catch (SchedulerException e) {
            log.error("暂停定时任务失败: {}", taskCode, e);
        }
    }

    /**
     * 恢复定时任务
     */
    public void resumeTask(String taskCode) {
        try {
            JobKey jobKey = new JobKey(taskCode, "MDM_TASKS");
            if (scheduler.checkExists(jobKey)) {
                scheduler.resumeJob(jobKey);
                log.info("定时任务已恢复: {}", taskCode);
            }
        } catch (SchedulerException e) {
            log.error("恢复定时任务失败: {}", taskCode, e);
        }
    }

    /**
     * 立即执行一次
     */
    public void executeNow(ScheduleTask task) throws SchedulerException {
        String jobName = task.getTaskCode() + "_MANUAL_" + System.currentTimeMillis();
        String jobGroup = "MDM_MANUAL";

        JobDetail jobDetail = JobBuilder.newJob(DynamicJob.class)
                .withIdentity(jobName, jobGroup)
                .usingJobData("taskId", task.getId())
                .usingJobData("taskCode", task.getTaskCode())
                .usingJobData("taskClass", task.getTaskClass())
                .usingJobData("taskParams", task.getTaskParams())
                .storeDurably(false)
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(jobName + "_TRIGGER", jobGroup)
                .startNow()
                .build();

        scheduler.scheduleJob(jobDetail, trigger);
        log.info("手动触发任务执行: {}", task.getTaskCode());
    }

    /**
     * 获取任务状态
     */
    public String getTaskState(String taskCode) {
        try {
            TriggerKey triggerKey = new TriggerKey(taskCode + "_TRIGGER", "MDM_TASKS");
            Trigger.TriggerState state = scheduler.getTriggerState(triggerKey);
            return state.name();
        } catch (SchedulerException e) {
            log.error("获取任务状态失败: {}", taskCode, e);
            return "UNKNOWN";
        }
    }

    /**
     * 动态任务执行类
     */
    public static class DynamicJob implements Job {

        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            JobDataMap dataMap = context.getJobDetail().getJobDataMap();

            Long taskId = dataMap.getLong("taskId");
            String taskCode = dataMap.getString("taskCode");
            String taskClass = dataMap.getString("taskClass");
            String taskParams = dataMap.getString("taskParams");

            log.info("执行定时任务: {} - {}", taskCode, new Date());

            try {
                // 这里可以根据taskClass动态加载执行类
                // 简化处理，直接记录日志
                log.info("任务参数: {}", taskParams);

                // TODO: 根据taskClass反射执行具体任务逻辑

            } catch (Exception e) {
                log.error("任务执行失败: {}", taskCode, e);
                throw new JobExecutionException(e);
            }
        }
    }
}

package com.vueadmin.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 定时任务DTO
 */
@Data
public class ScheduleTaskDto {

    private Long id;

    private String taskCode;

    private String taskName;

    private String taskType;

    private String taskParams;

    private String cronExpression;

    private String taskClass;

    private String status;

    private String lastExecuteTime;

    private String nextExecuteTime;

    private Integer executeCount;

    private String createdBy;

    private String createdAt;

    private String updatedBy;

    private String updatedAt;

    private String description;

    /**
     * Cron表达式说明
     */
    private String cronDescription;
}

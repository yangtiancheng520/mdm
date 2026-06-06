package com.vueadmin.entity.system;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 定时任务实体
 */
@Data
@Entity
@Table(name = "sys_schedule_task")
public class ScheduleTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_code", unique = true, nullable = false, length = 100)
    private String taskCode;

    @Column(name = "task_name", nullable = false, length = 200)
    private String taskName;

    @Column(name = "task_type", length = 50)
    private String taskType;

    @Column(name = "task_params", columnDefinition = "TEXT")
    private String taskParams;

    @Column(name = "cron_expression", length = 100)
    private String cronExpression;

    @Column(name = "task_class", length = 500)
    private String taskClass;

    @Column(length = 20)
    private String status = "active";

    @Column(name = "last_execute_time")
    private LocalDateTime lastExecuteTime;

    @Column(name = "next_execute_time")
    private LocalDateTime nextExecuteTime;

    @Column(name = "execute_count")
    private Integer executeCount = 0;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(columnDefinition = "TEXT")
    private String description;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = "active";
        if (executeCount == null) executeCount = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

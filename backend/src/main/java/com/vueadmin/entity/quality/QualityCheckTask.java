package com.vueadmin.entity.quality;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 检测任务实体
 */
@Data
@Entity
@Table(name = "qlt_check_task")
public class QualityCheckTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_code", nullable = false, length = 100)
    private String taskCode;

    @Column(name = "task_name", nullable = false, length = 200)
    private String taskName;

    @Column(name = "view_id")
    private Long viewId;

    @Column(name = "entity_ids", columnDefinition = "TEXT")
    private String entityIds;

    @Column(name = "rule_ids", columnDefinition = "TEXT")
    private String ruleIds;

    @Column(name = "status", length = 20)
    private String status = "pending";

    @Column(name = "total_records")
    private Integer totalRecords = 0;

    @Column(name = "total_rules")
    private Integer totalRules = 0;

    @Column(name = "pass_count")
    private Integer passCount = 0;

    @Column(name = "fail_count")
    private Integer failCount = 0;

    @Column(name = "pass_rate", precision = 5, scale = 2)
    private BigDecimal passRate;

    @Column(name = "main_total")
    private Integer mainTotal = 0;

    @Column(name = "main_pass")
    private Integer mainPass = 0;

    @Column(name = "main_fail")
    private Integer mainFail = 0;

    @Column(name = "sub_total")
    private Integer subTotal = 0;

    @Column(name = "sub_pass")
    private Integer subPass = 0;

    @Column(name = "sub_fail")
    private Integer subFail = 0;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "duration_ms")
    private Integer durationMs;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

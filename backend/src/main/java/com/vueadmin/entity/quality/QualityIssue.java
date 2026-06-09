package com.vueadmin.entity.quality;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 问题管理实体
 */
@Data
@Entity
@Table(name = "qlt_issue")
public class QualityIssue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "issue_code", nullable = false, length = 100)
    private String issueCode;

    @Column(name = "task_id")
    private Long taskId;

    @Column(name = "result_id")
    private Long resultId;

    @Column(name = "rule_id")
    private Long ruleId;

    @Column(name = "view_id")
    private Long viewId;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "entity_type", length = 20)
    private String entityType;

    @Column(name = "table_name", length = 100)
    private String tableName;

    @Column(name = "record_id")
    private Long recordId;

    @Column(name = "main_record_id")
    private Long mainRecordId;

    @Column(name = "field_code", length = 100)
    private String fieldCode;

    @Column(name = "field_value", columnDefinition = "TEXT")
    private String fieldValue;

    @Column(name = "issue_type", length = 50)
    private String issueType;

    @Column(name = "issue_level", length = 20)
    private String issueLevel;

    @Column(name = "issue_desc", columnDefinition = "TEXT")
    private String issueDesc;

    @Column(name = "status", length = 20)
    private String status = "open";

    @Column(name = "assignee", length = 50)
    private String assignee;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "resolved_by", length = 50)
    private String resolvedBy;

    @Column(name = "resolved_time")
    private LocalDateTime resolvedTime;

    @Column(name = "resolution", columnDefinition = "TEXT")
    private String resolution;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

package com.vueadmin.entity.quality;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 检测结果实体
 */
@Data
@Entity
@Table(name = "qlt_check_result")
public class QualityCheckResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Column(name = "rule_id", nullable = false)
    private Long ruleId;

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

    @Column(name = "is_passed")
    private Integer isPassed = 0;

    @Column(name = "issue_type", length = 50)
    private String issueType;

    @Column(name = "issue_level", length = 20)
    private String issueLevel;

    @Column(name = "issue_message", columnDefinition = "TEXT")
    private String issueMessage;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

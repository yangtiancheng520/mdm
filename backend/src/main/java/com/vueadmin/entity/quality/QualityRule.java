package com.vueadmin.entity.quality;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 质量规则实体
 */
@Data
@Entity
@Table(name = "qlt_rule")
public class QualityRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_code", nullable = false, length = 100)
    private String ruleCode;

    @Column(name = "rule_name", nullable = false, length = 200)
    private String ruleName;

    @Column(name = "rule_type", nullable = false, length = 50)
    private String ruleType;

    @Column(name = "view_id", nullable = false)
    private Long viewId;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(name = "entity_type", nullable = false, length = 20)
    private String entityType;

    @Column(name = "table_name", nullable = false, length = 100)
    private String tableName;

    @Column(name = "field_id")
    private Long fieldId;

    @Column(name = "field_code", length = 100)
    private String fieldCode;

    @Column(name = "field_name", length = 200)
    private String fieldName;

    @Column(name = "rule_config", columnDefinition = "TEXT")
    private String ruleConfig;

    @Column(name = "check_type", length = 50)
    private String checkType;

    @Column(name = "check_config", columnDefinition = "TEXT")
    private String checkConfig;

    @Column(name = "threshold", precision = 5, scale = 2)
    private BigDecimal threshold = new BigDecimal("100.00");

    @Column(name = "severity", length = 20)
    private String severity = "warning";

    @Column(name = "field_standard_id")
    private Long fieldStandardId;

    @Column(name = "value_domain_id")
    private Long valueDomainId;

    @Column(name = "template_id")
    private Long templateId;

    @Column(name = "status", length = 20)
    private String status = "active";

    @Column(name = "is_scheduled")
    private Boolean isScheduled = false;

    @Column(name = "cron_expression", length = 100)
    private String cronExpression;

    @Column(name = "last_check_time")
    private LocalDateTime lastCheckTime;

    @Column(name = "last_pass_rate", precision = 5, scale = 2)
    private BigDecimal lastPassRate;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

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

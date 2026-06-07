package com.vueadmin.entity.standard;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 校验规则
 */
@Data
@Entity
@Table(name = "std_view_validation")
public class ViewValidation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "view_id", nullable = false)
    private Long viewId;

    @Column(name = "rule_code", nullable = false, length = 100)
    private String ruleCode;

    @Column(name = "rule_name", nullable = false, length = 200)
    private String ruleName;

    @Column(name = "rule_type", nullable = false, length = 50)
    private String ruleType; // cross-跨字段 custom-自定义

    // 触发配置
    @Column(name = "trigger_entity_id")
    private Long triggerEntityId;

    @Column(name = "trigger_field_id")
    private Long triggerFieldId;

    @Column(name = "trigger_condition", columnDefinition = "TEXT")
    private String triggerCondition;

    // 目标配置
    @Column(name = "target_entity_id")
    private Long targetEntityId;

    @Column(name = "target_field_id")
    private Long targetFieldId;

    @Column(name = "action", length = 50)
    private String action; // required/readonly/hidden/setvalue

    @Column(name = "action_value", columnDefinition = "TEXT")
    private String actionValue;

    // 提示
    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "error_level", length = 20)
    private String errorLevel = "error";

    @Column(name = "status", length = 20)
    private String status = "active";

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_by", length = 50)
    private String updatedBy;

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

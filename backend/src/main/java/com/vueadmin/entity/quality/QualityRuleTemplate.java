package com.vueadmin.entity.quality;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 质量规则模板实体
 */
@Data
@Entity
@Table(name = "qlt_rule_template")
public class QualityRuleTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "template_code", nullable = false, length = 100, unique = true)
    private String templateCode;

    @Column(name = "template_name", nullable = false, length = 200)
    private String templateName;

    @Column(name = "template_type", length = 50)
    private String templateType;

    @Column(name = "check_type", length = 50)
    private String checkType;

    @Column(name = "check_config", columnDefinition = "TEXT")
    private String checkConfig;

    @Column(name = "severity", length = 20)
    private String severity = "warning";

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "status", length = 20)
    private String status = "active";

    /**
     * 是否系统模板（系统模板不可编辑删除）
     */
    @Column(name = "is_system")
    private Boolean isSystem = false;

    /**
     * 模板分类
     */
    @Column(name = "category", length = 50)
    private String category;

    /**
     * 标签，逗号分隔
     */
    @Column(name = "tags", length = 200)
    private String tags;

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

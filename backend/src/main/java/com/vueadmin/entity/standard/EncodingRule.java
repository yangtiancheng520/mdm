package com.vueadmin.entity.standard;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 编码规则实体
 * 用于定义灵活的编码生成规则，支持多种规则段类型
 */
@Data
@Entity
@Table(name = "std_encoding_rule")
public class EncodingRule {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 规则编码，唯一标识
     */
    @Column(name = "rule_code", unique = true, nullable = false, length = 100)
    private String ruleCode;

    /**
     * 规则名称
     */
    @Column(name = "rule_name", nullable = false, length = 200)
    private String ruleName;

    /**
     * 规则定义JSON
     * 包含规则段配置和全局设置
     */
    @Column(name = "rule_definition", columnDefinition = "TEXT")
    private String ruleDefinition;

    /**
     * 适用范围类型
     * global: 全局通用
     * category: 按分类
     * instance: 按实例
     */
    @Column(name = "scope_type", length = 50)
    private String scopeType;

    /**
     * 范围配置JSON
     */
    @Column(name = "scope_config", columnDefinition = "TEXT")
    private String scopeConfig;

    /**
     * 状态
     * active: 启用
     * inactive: 停用
     */
    @Column(name = "status", length = 20)
    private String status = "active";

    /**
     * 示例
     */
    @Column(name = "preview_example", length = 200)
    private String example;

    /**
     * 描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 创建人
     */
    @Column(name = "created_by", length = 50)
    private String createdBy;

    /**
     * 创建时间
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * 更新人
     */
    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    /**
     * 更新时间
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = "active";
        if (scopeType == null) scopeType = "global";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

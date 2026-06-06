package com.vueadmin.entity.standard;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 字段标准实体
 * 用于定义系统中的标准字段规范，包括字段类型、验证规则等
 */
@Data
@Entity
@Table(name = "std_field_standard")
public class FieldStandard {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 字段编码，唯一标识
     */
    @Column(name = "field_code", unique = true, nullable = false, length = 100)
    private String fieldCode;

    /**
     * 字段名称
     */
    @Column(name = "field_name", nullable = false, length = 100)
    private String fieldName;

    /**
     * 字段类型
     * 支持类型：string/number/date/datetime/boolean/text/select/multi_select
     */
    @Column(name = "field_type", nullable = false, length = 50)
    private String fieldType;

    /**
     * 字段长度（适用于string类型）
     */
    @Column(name = "length")
    private Integer length;

    /**
     * 小数精度（适用于number类型）
     */
    @Column(name = "`precision`")
    private Integer precision;

    /**
     * 默认值
     */
    @Column(name = "default_value", length = 500)
    private String defaultValue;

    /**
     * 是否必填
     */
    @Column(name = "is_required")
    private Integer isRequired = 0;

    /**
     * 验证规则（JSON格式）
     * 可包含正则表达式、范围限制等
     */
    @Column(name = "validation_rule", columnDefinition = "TEXT")
    private String validationRule;

    /**
     * 引用ID（关联其他标准或数据源）
     */
    @Column(name = "reference_id")
    private Long referenceId;

    /**
     * 引用来源
     */
    @Column(name = "reference_source", length = 50)
    private String referenceSource;

    /**
     * 分类ID（关联字段分类表）
     */
    @Column(name = "category_id")
    private Long categoryId;

    /**
     * 分类名称（冗余字段，便于显示，后续可删除category字段）
     */
    @Column(name = "category", length = 50)
    private String category;

    /**
     * 状态
     * draft: 草稿
     * published: 已发布
     * archived: 已归档
     */
    @Column(name = "status", length = 20)
    private String status = "draft";

    /**
     * 版本号
     */
    @Column(name = "version")
    private Integer version = 1;

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

    /**
     * 描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 持久化前回调
     * 设置创建时间和更新时间
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = "draft";
        if (version == null) version = 1;
        if (isRequired == null) isRequired = 0;
    }

    /**
     * 更新前回调
     * 设置更新时间
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

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
     * string: 字符
     * integer: 整型
     * boolean: 布尔
     * decimal: 浮点型
     * date: 日期
     * datetime: 日期时间
     * time: 时间
     * text: 长文本
     */
    @Column(name = "field_type", nullable = false, length = 50)
    private String fieldType;

    /**
     * 字段长度（适用于string/decimal类型）
     */
    @Column(name = "length")
    private Integer length;

    /**
     * 小数精度（适用于decimal类型）
     */
    @Column(name = "`precision`")
    private Integer precision;

    /**
     * 默认值
     */
    @Column(name = "default_value", length = 500)
    private String defaultValue;

    /**
     * 是否关联值域
     * 0: 否
     * 1: 是
     */
    @Column(name = "is_enum")
    private Integer isEnum = 0;

    /**
     * 值域ID（关联值域表，用于枚举类型字段）
     * 当isEnum为1时，关联值域获取选项列表
     */
    @Column(name = "domain_id")
    private Long domainId;

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
     * active: 启用
     * inactive: 停用
     */
    @Column(name = "status", length = 20)
    private String status = "active";

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
        if (status == null) status = "active";
        if (version == null) version = 1;
        if (isEnum == null) isEnum = 0;
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

package com.vueadmin.entity.standard;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 字段分类实体
 * 用于管理字段标准库的分类，支持树形结构
 */
@Data
@Entity
@Table(name = "std_field_category")
public class FieldCategory {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 分类编码，唯一标识
     */
    @Column(name = "category_code", unique = true, nullable = false, length = 50)
    private String categoryCode;

    /**
     * 分类名称
     */
    @Column(name = "category_name", nullable = false, length = 100)
    private String categoryName;

    /**
     * 父级分类ID（支持树形结构）
     */
    @Column(name = "parent_id")
    private Long parentId;

    /**
     * 排序
     */
    @Column(name = "sort")
    private Integer sort = 0;

    /**
     * 状态
     * active: 启用
     * inactive: 停用
     */
    @Column(name = "status", length = 20)
    private String status = "active";

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
     * 子分类（非数据库字段，用于树形结构）
     */
    @Transient
    private java.util.List<FieldCategory> children;

    /**
     * 持久化前回调
     * 设置创建时间和更新时间
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = "active";
        if (sort == null) sort = 0;
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

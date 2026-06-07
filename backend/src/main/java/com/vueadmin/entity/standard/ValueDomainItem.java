package com.vueadmin.entity.standard;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 值域项实体
 * 存储值域中的具体选项值
 */
@Data
@Entity
@Table(name = "std_value_domain_item")
public class ValueDomainItem {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 值域ID
     */
    @Column(name = "domain_id", nullable = false)
    private Long domainId;

    /**
     * 值
     */
    @Column(name = "item_value", length = 200)
    private String itemValue;

    /**
     * 父级项ID（用于树形结构）
     */
    @Column(name = "parent_item_id")
    private Long parentItemId;

    /**
     * 排序
     */
    @Column(name = "sort")
    private Integer sort = 0;

    /**
     * 状态
     * 启用: 启用
     * 停用: 停用
     */
    @Column(name = "status", length = 20)
    private String status = "启用";

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

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = "启用";
        if (sort == null) sort = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

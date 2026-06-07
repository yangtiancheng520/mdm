package com.vueadmin.entity.standard;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 实体定义（主实体/子实体）
 */
@Data
@Entity
@Table(name = "std_view_entity")
public class ViewEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "view_id", nullable = false)
    private Long viewId;

    @Column(name = "entity_code", nullable = false, length = 100)
    private String entityCode;

    @Column(name = "entity_name", nullable = false, length = 200)
    private String entityName;

    @Column(name = "entity_type", nullable = false, length = 20)
    private String entityType; // main-主实体 sub-子实体

    // 子实体配置
    @Column(name = "sort")
    private Integer sort = 0;

    @Column(name = "min_rows")
    private Integer minRows = 0;

    @Column(name = "max_rows")
    private Integer maxRows;

    // 功能配置
    @Column(name = "enable_add")
    private Boolean enableAdd = true;

    @Column(name = "enable_delete")
    private Boolean enableDelete = true;

    @Column(name = "enable_copy")
    private Boolean enableCopy = false;

    @Column(name = "enable_sort")
    private Boolean enableSort = false;

    @Column(name = "is_inherited")
    private Boolean isInherited = false;  // 是否继承实体（修订时）

    @Column(name = "status", length = 20)
    private String status = "active";

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

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

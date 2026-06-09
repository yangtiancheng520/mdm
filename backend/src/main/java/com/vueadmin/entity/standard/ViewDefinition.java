package com.vueadmin.entity.standard;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 视图定义实体
 */
@Data
@Entity
@Table(name = "std_view")
public class ViewDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "view_code", nullable = false, length = 100)
    private String viewCode;

    @Column(name = "view_name", nullable = false, length = 200)
    private String viewName;

    @Column(name = "category_id")
    private Long categoryId;

    // 版本管理
    @Column(name = "version")
    private Integer version = 1;

    @Column(name = "base_version_id")
    private Long baseVersionId;

    @Column(name = "is_trunk")
    private Boolean isTrunk = true;  // 是否主干版本

    @Column(name = "is_latest")
    private Boolean isLatest = true;

    // 布局配置
    @Column(name = "layout_columns")
    private Integer layoutColumns = 2;

    @Column(name = "label_width")
    private Integer labelWidth = 100;

    // 功能开关
    @Column(name = "enable_copy")
    private Boolean enableCopy = true;

    @Column(name = "enable_import")
    private Boolean enableImport = false;

    @Column(name = "enable_export")
    private Boolean enableExport = true;

    // 状态: draft(草稿), pending_approval(审批中), published(已发布), disabled(已停用), revising(修订中), history(历史版本)
    @Column(name = "status", length = 20)
    private String status = "draft";

    @Column(name = "publish_time")
    private LocalDateTime publishTime;

    @Column(name = "table_name", length = 100)
    private String tableName;

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

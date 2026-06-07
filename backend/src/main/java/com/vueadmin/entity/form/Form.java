package com.vueadmin.entity.form;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 表单实体
 */
@Data
@Entity
@Table(name = "frm_form")
public class Form {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "form_code", nullable = false, length = 100, unique = true)
    private String formCode;

    @Column(name = "form_name", nullable = false, length = 200)
    private String formName;

    @Column(name = "form_type", nullable = false, length = 50)
    private String formType; // create/edit/view/search

    @Column(name = "view_id")
    private Long viewId;

    // 设计模式
    @Column(name = "design_mode", length = 20)
    private String designMode = "auto"; // auto/blank

    @Column(name = "style_template", length = 50)
    private String styleTemplate; // single/dual/triple/master-detail/grouped/tabs

    // 布局配置
    @Column(name = "layout_config", columnDefinition = "TEXT")
    private String layoutConfig;

    // 功能开关
    @Column(name = "enable_copy")
    private Boolean enableCopy = false;

    @Column(name = "enable_import")
    private Boolean enableImport = false;

    @Column(name = "enable_export")
    private Boolean enableExport = false;

    // 状态
    @Column(name = "status", length = 20)
    private String status = "draft";

    @Column(name = "is_default")
    private Boolean isDefault = false;

    @Column(name = "version")
    private Integer version = 1;

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

package com.vueadmin.entity.form;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 表单组件实体
 */
@Data
@Entity
@Table(name = "frm_component")
public class FormComponent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "form_id", nullable = false)
    private Long formId;

    // 组件来源
    @Column(name = "view_field_id")
    private Long viewFieldId;

    @Column(name = "field_code", nullable = false, length = 100)
    private String fieldCode;

    @Column(name = "field_name", nullable = false, length = 200)
    private String fieldName;

    @Column(name = "field_type", nullable = false, length = 50)
    private String fieldType;

    @Column(name = "domain_id")
    private Long domainId;

    // 布局位置
    @Column(name = "group_id")
    private Long groupId;

    @Column(name = "row_index", nullable = false)
    private Integer rowIndex = 0;

    @Column(name = "col_index", nullable = false)
    private Integer colIndex = 0;

    @Column(name = "col_span")
    private Integer colSpan = 1;

    @Column(name = "row_span")
    private Integer rowSpan = 1;

    // 字段属性
    @Column(name = "is_required")
    private Boolean isRequired = false;

    @Column(name = "is_readonly")
    private Boolean isReadonly = false;

    @Column(name = "is_hidden")
    private Boolean isHidden = false;

    @Column(name = "default_value", columnDefinition = "TEXT")
    private String defaultValue;

    @Column(name = "placeholder", length = 200)
    private String placeholder;

    // 校验配置
    @Column(name = "validation_rules", columnDefinition = "TEXT")
    private String validationRules;

    // 样式配置
    @Column(name = "label_width")
    private Integer labelWidth;

    @Column(name = "component_width", length = 50)
    private String componentWidth;

    @Column(name = "sort")
    private Integer sort = 0;

    @Column(name = "status", length = 20)
    private String status = "active";

    @Column(name = "created_at")
    private LocalDateTime createdAt;

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

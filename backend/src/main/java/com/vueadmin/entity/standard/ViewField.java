package com.vueadmin.entity.standard;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 视图字段
 */
@Data
@Entity
@Table(name = "std_view_field")
public class ViewField {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(name = "field_code", nullable = false, length = 100)
    private String fieldCode;

    @Column(name = "field_name", nullable = false, length = 200)
    private String fieldName;

    // 字段来源
    @Column(name = "field_standard_id")
    private Long fieldStandardId;

    // 基础属性
    @Column(name = "field_type", length = 50)
    private String fieldType;

    @Column(name = "length")
    private Integer length;

    @Column(name = "precision_val")
    private Integer precisionVal;

    // 值域配置
    @Column(name = "domain_id")
    private Long domainId;

    @Column(name = "domain_code", length = 100)
    private String domainCode;

    @Column(name = "domain_name", length = 200)
    private String domainName;

    // 分组与排序
    @Column(name = "group_id")
    private Long groupId;

    @Column(name = "sort")
    private Integer sort = 0;

    @Column(name = "column_span")
    private Integer columnSpan = 1;

    // 字段属性
    @Column(name = "is_required")
    private Boolean isRequired = false;

    @Column(name = "is_readonly")
    private Boolean isReadonly = false;

    @Column(name = "is_hidden")
    private Boolean isHidden = false;

    @Column(name = "is_unique")
    private Boolean isUnique = false;

    @Column(name = "is_query")
    private Boolean isQuery = false;

    @Column(name = "is_query_result")
    private Boolean isQueryResult = true;

    @Column(name = "is_sortable")
    private Boolean isSortable = false;

    @Column(name = "is_inherited")
    private Boolean isInherited = false;  // 是否继承字段（修订时）

    // 自动编号配置
    @Column(name = "auto_number")
    private Boolean autoNumber = false;

    @Column(name = "encoding_rule_id")
    private Long encodingRuleId;

    @Column(name = "encoding_rule_name", length = 200)
    private String encodingRuleName;

    // 默认值
    @Column(name = "default_value", columnDefinition = "TEXT")
    private String defaultValue;

    @Column(name = "default_value_type", length = 50)
    private String defaultValueType = "constant";

    // 参照配置
    @Column(name = "ref_source", length = 50)
    private String refSource;

    @Column(name = "ref_id")
    private Long refId;

    @Column(name = "ref_filter", columnDefinition = "TEXT")
    private String refFilter;

    @Column(name = "ref_cascade_field", length = 100)
    private String refCascadeField;

    // 枚举配置
    @Column(name = "enum_code", length = 100)
    private String enumCode;

    // 校验配置
    @Column(name = "min_length")
    private Integer minLength;

    @Column(name = "max_length")
    private Integer maxLength;

    @Column(name = "min_value", precision = 20, scale = 4)
    private BigDecimal minValue;

    @Column(name = "max_value", precision = 20, scale = 4)
    private BigDecimal maxValue;

    @Column(name = "regex_pattern", length = 500)
    private String regexPattern;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    // 联动配置
    @Column(name = "link_config", columnDefinition = "TEXT")
    private String linkConfig;

    // 其他
    @Column(name = "placeholder", length = 200)
    private String placeholder;

    @Column(name = "tooltip", columnDefinition = "TEXT")
    private String tooltip;

    @Column(name = "status", length = 20)
    private String status = "active";

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

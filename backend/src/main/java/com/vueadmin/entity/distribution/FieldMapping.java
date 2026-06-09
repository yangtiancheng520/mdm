package com.vueadmin.entity.distribution;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 字段映射配置实体
 */
@Data
@Entity
@Table(name = "dis_field_mapping")
public class FieldMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "data_type", nullable = false, length = 50)
    private String dataType;

    @Column(name = "data_type_name", length = 100)
    private String dataTypeName;

    @Column(name = "system_config_id")
    private Long systemConfigId;

    @Column(name = "mdm_field", nullable = false, length = 100)
    private String mdmField;

    @Column(name = "mdm_field_name", length = 200)
    private String mdmFieldName;

    @Column(name = "sap_field", nullable = false, length = 100)
    private String sapField;

    @Column(name = "sap_field_name", length = 200)
    private String sapFieldName;

    @Column(name = "sap_structure", length = 100)
    private String sapStructure;

    @Column(name = "field_type", length = 20)
    private String fieldType = "STRING";

    @Column(name = "is_required")
    private Integer isRequired = 0;

    @Column(name = "is_key")
    private Integer isKey = 0;

    @Column(name = "transform_type", length = 20)
    private String transformType = "DIRECT";

    @Column(name = "transform_rule", columnDefinition = "TEXT")
    private String transformRule;

    @Column(name = "default_value", length = 200)
    private String defaultValue;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

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

    @Column(name = "remark", length = 200)
    private String remark;

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

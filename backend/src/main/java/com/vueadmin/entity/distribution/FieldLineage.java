package com.vueadmin.entity.distribution;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 字段级血缘日志实体
 */
@Data
@Entity
@Table(name = "dis_field_lineage")
public class FieldLineage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "log_id", nullable = false)
    private Long logId;

    @Column(name = "data_type", nullable = false, length = 50)
    private String dataType;

    @Column(name = "data_id", nullable = false)
    private Long dataId;

    // 发送方字段
    @Column(name = "mdm_field", nullable = false, length = 100)
    private String mdmField;

    @Column(name = "mdm_field_name", length = 200)
    private String mdmFieldName;

    // 消费方字段
    @Column(name = "sap_field", nullable = false, length = 100)
    private String sapField;

    @Column(name = "sap_field_name", length = 200)
    private String sapFieldName;

    // 字段值
    @Column(name = "source_value", columnDefinition = "TEXT")
    private String sourceValue;

    @Column(name = "target_value", columnDefinition = "TEXT")
    private String targetValue;

    // 转换信息
    @Column(name = "transform_type", length = 20)
    private String transformType;

    @Column(name = "transform_rule", columnDefinition = "TEXT")
    private String transformRule;

    // 状态
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "error_msg", length = 500)
    private String errorMsg;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

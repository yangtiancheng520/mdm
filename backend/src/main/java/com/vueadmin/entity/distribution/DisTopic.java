package com.vueadmin.entity.distribution;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 分发主题实体
 * 定义分发任务的主题，如"供应商分发到SAP"、"物料分发到MES"等
 */
@Data
@Entity
@Table(name = "dis_topic")
public class DisTopic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "topic_code", nullable = false, length = 50, unique = true)
    private String topicCode;

    @Column(name = "topic_name", nullable = false, length = 100)
    private String topicName;

    @Column(name = "data_type", nullable = false, length = 50)
    private String dataType;

    @Column(name = "data_type_name", length = 100)
    private String dataTypeName;

    @Column(name = "distribute_mode", length = 20)
    private String distributeMode = "MANUAL"; // MANUAL-手动 / REALTIME-实时 / SCHEDULE-定时

    @Column(name = "cron_expression", length = 100)
    private String cronExpression;

    @Column(name = "batch_size")
    private Integer batchSize = 100;

    @Column(name = "retry_count")
    private Integer retryCount = 3;

    @Column(name = "retry_interval")
    private Integer retryInterval = 5000; // 重试间隔(毫秒)

    @Column(name = "filter_condition", columnDefinition = "TEXT")
    private String filterCondition; // 数据过滤条件JSON

    @Column(name = "status", length = 20)
    private String status = "inactive"; // active-启用 / inactive-停用

    @Column(name = "description", length = 500)
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

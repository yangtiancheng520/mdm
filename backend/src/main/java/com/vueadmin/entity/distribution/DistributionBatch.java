package com.vueadmin.entity.distribution;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 分发批次实体
 */
@Data
@Entity
@Table(name = "dis_distribution_batch")
public class DistributionBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_code", nullable = false, length = 100, unique = true)
    private String batchCode;

    @Column(name = "data_type", nullable = false, length = 50)
    private String dataType;

    @Column(name = "system_config_id")
    private Long systemConfigId;

    @Column(name = "total_count")
    private Integer totalCount = 0;

    @Column(name = "success_count")
    private Integer successCount = 0;

    @Column(name = "failed_count")
    private Integer failedCount = 0;

    @Column(name = "pending_count")
    private Integer pendingCount = 0;

    @Column(name = "status", length = 20)
    private String status = "pending";

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "duration_ms")
    private Integer durationMs;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

package com.vueadmin.entity.distribution;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 分发订阅实体
 * 定义目标系统对分发主题的订阅关系
 */
@Data
@Entity
@Table(name = "dis_subscription")
public class DisSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "topic_id", nullable = false)
    private Long topicId;

    @Column(name = "topic_name", length = 100)
    private String topicName;

    @Column(name = "system_config_id", nullable = false)
    private Long systemConfigId;

    @Column(name = "system_config_name", length = 100)
    private String systemConfigName;

    @Column(name = "system_type", length = 20)
    private String systemType;

    @Column(name = "mapping_id")
    private Long mappingId; // 关联的字段映射配置ID

    @Column(name = "mapping_name", length = 100)
    private String mappingName;

    @Column(name = "priority")
    private Integer priority = 0; // 分发优先级，数字越小优先级越高

    @Column(name = "sync_mode", length = 20)
    private String syncMode = "SYNC"; // SYNC-同步 / ASYNC-异步

    @Column(name = "enable_create")
    private Integer enableCreate = 1; // 是否分发新增数据

    @Column(name = "enable_update")
    private Integer enableUpdate = 1; // 是否分发更新数据

    @Column(name = "enable_delete")
    private Integer enableDelete = 0; // 是否分发删除数据

    @Column(name = "filter_rule", columnDefinition = "TEXT")
    private String filterRule; // 订阅过滤规则JSON

    @Column(name = "status", length = 20)
    private String status = "active";

    @Column(name = "last_sync_time")
    private LocalDateTime lastSyncTime;

    @Column(name = "last_sync_status", length = 20)
    private String lastSyncStatus;

    @Column(name = "last_sync_count")
    private Integer lastSyncCount;

    @Column(name = "total_sync_count")
    private Integer totalSyncCount = 0;

    @Column(name = "total_success_count")
    private Integer totalSuccessCount = 0;

    @Column(name = "total_fail_count")
    private Integer totalFailCount = 0;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "remark", length = 500)
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

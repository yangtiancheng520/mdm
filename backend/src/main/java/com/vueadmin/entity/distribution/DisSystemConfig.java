package com.vueadmin.entity.distribution;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 目标系统连接配置实体
 */
@Data
@Entity
@Table(name = "dis_system_config")
public class DisSystemConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "config_name", nullable = false, length = 100)
    private String configName;

    @Column(name = "config_code", nullable = false, length = 50, unique = true)
    private String configCode;

    @Column(name = "system_type", nullable = false, length = 20)
    private String systemType;

    @Column(name = "system_type_name", length = 50)
    private String systemTypeName;

    @Column(name = "connection_config", nullable = false, columnDefinition = "TEXT")
    private String connectionConfig;

    @Column(name = "pool_size")
    private Integer poolSize = 10;

    @Column(name = "timeout")
    private Integer timeout = 30000;

    @Column(name = "status", length = 20)
    private String status = "inactive";

    @Column(name = "is_default")
    private Integer isDefault = 0;

    @Column(name = "last_test_time")
    private LocalDateTime lastTestTime;

    @Column(name = "last_test_result", length = 20)
    private String lastTestResult;

    @Column(name = "last_test_msg", length = 500)
    private String lastTestMsg;

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

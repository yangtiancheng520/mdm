package com.vueadmin.entity.system;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 外部集成配置实体
 */
@Data
@Entity
@Table(name = "sys_integration_config")
public class IntegrationConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "integration_code", unique = true, nullable = false, length = 100)
    private String integrationCode;

    @Column(name = "integration_name", nullable = false, length = 200)
    private String integrationName;

    @Column(name = "integration_type", length = 50)
    private String integrationType;

    @Column(name = "api_endpoint", length = 500)
    private String apiEndpoint;

    @Column(name = "auth_type", length = 50)
    private String authType;

    @Column(name = "auth_config", columnDefinition = "TEXT")
    private String authConfig;

    @Column(name = "request_config", columnDefinition = "TEXT")
    private String requestConfig;

    @Column(name = "mapping_config", columnDefinition = "TEXT")
    private String mappingConfig;

    @Column(length = 20)
    private String status = "active";

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(columnDefinition = "TEXT")
    private String description;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = "active";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

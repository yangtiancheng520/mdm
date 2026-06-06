package com.vueadmin.entity.system;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 数据权限实体
 */
@Data
@Entity
@Table(name = "sys_data_permission")
public class DataPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role_id", nullable = false)
    private Long roleId;

    @Column(name = "data_type", length = 50)
    private String dataType;

    @Column(name = "permission_type", length = 50, nullable = false)
    private String permissionType;

    @Column(name = "org_ids", columnDefinition = "TEXT")
    private String orgIds;

    @Column(name = "custom_rule", columnDefinition = "TEXT")
    private String customRule;

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

package com.vueadmin.entity.standard;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 数据标准视图实体
 */
@Data
@Entity
@Table(name = "std_data_standard")
public class DataStandard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "standard_code", nullable = false, unique = true, length = 100)
    private String standardCode;

    @Column(name = "standard_name", nullable = false, length = 200)
    private String standardName;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "fields_definition", columnDefinition = "TEXT")
    private String fieldsDefinition;

    @Column(name = "status", length = 20)
    private String status = "draft";

    @Column(name = "version")
    private Integer version = 1;

    @Column(name = "publish_time")
    private LocalDateTime publishTime;

    @Column(name = "approval_status", length = 20)
    private String approvalStatus = "pending";

    @Column(name = "approval_by", length = 50)
    private String approvalBy;

    @Column(name = "approval_time")
    private LocalDateTime approvalTime;

    @Column(name = "approval_comment", columnDefinition = "TEXT")
    private String approvalComment;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

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

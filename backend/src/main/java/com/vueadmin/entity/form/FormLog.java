package com.vueadmin.entity.form;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 表单操作日志实体
 */
@Data
@Entity
@Table(name = "log_form")
public class FormLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "form_id", nullable = false)
    private Long formId;

    // 操作信息
    @Column(name = "operation_type", nullable = false, length = 50)
    private String operationType; // create/update/delete/publish/unpublish/save_design

    @Column(name = "operation_detail", length = 500)
    private String operationDetail;

    // 版本信息
    @Column(name = "from_version")
    private Integer fromVersion;

    @Column(name = "to_version")
    private Integer toVersion;

    // 状态变更
    @Column(name = "from_status", length = 20)
    private String fromStatus;

    @Column(name = "to_status", length = 20)
    private String toStatus;

    // 变更数据快照
    @Column(name = "change_data", columnDefinition = "TEXT")
    private String changeData;

    // 审计信息
    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

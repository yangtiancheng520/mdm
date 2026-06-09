package com.vueadmin.entity.data;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 数据操作日志实体
 */
@Data
@Entity
@Table(name = "log_md_operation")
public class DataOperationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 分类ID
     */
    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    /**
     * 表单ID
     */
    @Column(name = "form_id", nullable = false)
    private Long formId;

    /**
     * 视图ID
     */
    @Column(name = "view_id")
    private Long viewId;

    /**
     * 操作类型：create/update/delete/submit/approve/reject/quality_check/qc_pass/qc_fail/obsolete/restore/distribute
     */
    @Column(name = "operation_type", length = 50)
    private String operationType;

    /**
     * 操作详情
     */
    @Column(name = "operation_detail", length = 500)
    private String operationDetail;

    /**
     * 原状态
     */
    @Column(name = "from_status", length = 50)
    private String fromStatus;

    /**
     * 目标状态
     */
    @Column(name = "to_status", length = 50)
    private String toStatus;

    /**
     * 主表记录ID
     */
    @Column(name = "main_record_id")
    private Long mainRecordId;

    /**
     * 操作数据快照JSON
     */
    @Column(name = "operation_data", columnDefinition = "TEXT")
    private String operationData;

    /**
     * 质检评分
     */
    @Column(name = "quality_score", precision = 5, scale = 2)
    private BigDecimal qualityScore;

    /**
     * 质检问题详情JSON
     */
    @Column(name = "quality_issues", columnDefinition = "TEXT")
    private String qualityIssues;

    /**
     * 操作原因（驳回、作废、恢复等）
     */
    @Column(name = "operation_reason", columnDefinition = "TEXT")
    private String operationReason;

    /**
     * 状态：active(生效) / obsolete(作废)
     */
    @Column(name = "status", length = 20)
    private String status;

    /**
     * 创建人
     */
    @Column(name = "created_by", length = 50)
    private String createdBy;

    /**
     * 创建时间
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * 更新人
     */
    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    /**
     * 更新时间
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * IP地址
     */
    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    /**
     * 用户代理
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = "active";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

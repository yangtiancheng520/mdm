package com.vueadmin.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 数据操作日志DTO
 */
@Data
public class DataOperationLogDto {

    private Long id;
    private Long categoryId;
    private Long formId;
    private String formName;
    private Long viewId;
    private String operationType;      // 操作类型
    private String operationDetail;    // 操作详情
    private String fromStatus;         // 原状态
    private String toStatus;           // 目标状态
    private Long mainRecordId;
    private String operationData;
    private BigDecimal qualityScore;   // 质检评分
    private String qualityIssues;      // 质检问题JSON
    private String operationReason;    // 操作原因
    private String status;
    private String createdBy;
    private String createdAt;
    private String updatedBy;
    private String updatedAt;
    private String ipAddress;
    private String userAgent;
}

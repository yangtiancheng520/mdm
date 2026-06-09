package com.vueadmin.dto;

import lombok.Data;

/**
 * 表单操作日志DTO
 */
@Data
public class FormLogDto {

    private Long id;
    private Long formId;

    // 操作信息
    private String operationType;
    private String operationDetail;

    // 版本信息
    private Integer fromVersion;
    private Integer toVersion;
    private String versionDisplay; // 显示用，如 "V1 → V2"

    // 状态变更
    private String fromStatus;
    private String toStatus;
    private String statusDisplay; // 显示用，如 "草稿 → 已发布"

    // 变更数据
    private String changeData;

    // 审计信息
    private String createdBy;
    private String createdAt;
    private String ipAddress;
}

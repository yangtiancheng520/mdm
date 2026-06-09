package com.vueadmin.dto.distribution;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 血缘汇总DTO - 用于列表展示
 */
@Data
public class LineageSummaryDTO {

    // 基本信息
    private Long logId;
    private String logCode;
    private Long dataId;
    private String dataCode;
    private String dataName;
    private String dataType;
    private String formName;  // 表单名

    // 系统信息
    private Long systemConfigId;
    private String systemConfigName;
    private String systemType;
    private String interfaceName;

    // 操作信息
    private String operation;
    private String operationName;

    // 状态与时间
    private String sendStatus;
    private String statusName;
    private LocalDateTime sendTime;
    private LocalDateTime receiveTime;
    private Integer durationMs;

    // 目标信息
    private String targetKey;

    // 字段统计
    private Integer fieldCount;
    private Integer successFieldCount;
    private Integer failedFieldCount;

    // 错误信息
    private String errorCode;
    private String errorMsg;

    // 审计信息
    private String createdBy;
    private LocalDateTime createdAt;
}

package com.vueadmin.dto.distribution;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 血缘详情DTO
 */
@Data
public class LineageDetailDTO {

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
    private LocalDateTime sendTime;
    private LocalDateTime receiveTime;
    private Integer durationMs;

    // 目标信息
    private String targetKey;

    // 字段统计
    private Integer fieldCount;
    private Integer successFieldCount;

    // SAP信息
    private String sapReturnCode;
    private String sapMessageType;
    private String sapMessage;

    // 错误信息
    private String errorCode;
    private String errorMsg;

    // 审计信息
    private String createdBy;
    private LocalDateTime createdAt;

    // 字段级血缘列表
    private List<FieldLineageDTO> fieldLineages;

    /**
     * 字段级血缘DTO
     */
    @Data
    public static class FieldLineageDTO {
        private Long id;
        private Integer sequenceNo;  // 序号

        private String mdmField;        // 发送方字段编码
        private String mdmFieldName;    // 发送方字段名
        private String sapField;        // 消费方字段编码
        private String sapFieldName;    // 消费方字段名

        private String sourceValue;     // 源值
        private String targetValue;     // 目标值

        private String transformType;   // 转换类型
        private String transformTypeName; // 转换类型名称
        private String transformRule;   // 转换规则

        private String status;          // 状态
        private String statusName;      // 状态名称
        private String errorMsg;        // 错误信息
    }
}

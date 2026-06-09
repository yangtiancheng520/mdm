package com.vueadmin.dto.distribution;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 字段级血缘DTO - 用于列表展示
 */
@Data
public class FieldLineageFlatDTO {

    // 日志信息
    private Long logId;
    private String logCode;

    // 数据信息
    private Long dataId;
    private String dataCode;
    private String dataName;
    private String dataType;
    private String formName;  // 表单名

    // 系统信息
    private String systemConfigName;

    // 字段信息
    private String mdmField;        // 发送方字段编码
    private String mdmFieldName;    // 发送方字段名
    private String sapField;        // 接收方字段编码
    private String sapFieldName;    // 接收方字段名

    // 字段值
    private String sourceValue;     // 源值
    private String targetValue;     // 目标值

    // 转换信息
    private String transformType;   // 转换类型
    private String transformTypeName; // 转换类型名称
    private String transformRule;   // 转换规则

    // 状态
    private String fieldStatus;     // 字段状态
    private String fieldStatusName; // 字段状态名称

    // 时间
    private LocalDateTime sendTime; // 发送时间
    private LocalDateTime receiveTime; // 接收时间

    // 操作信息
    private String operation;       // 操作类型
    private String operationName;   // 操作名称

    // 整体状态
    private String overallStatus;   // 整体发送状态
    private String overallStatusName; // 整体状态名称
}

package com.vueadmin.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 校验规则DTO
 */
@Data
public class ViewValidationDto {

    private Long id;
    private Long viewId;
    private String ruleCode;
    private String ruleName;
    private String ruleType; // cross-跨字段 custom-自定义

    // 触发配置
    private Long triggerEntityId;
    private String triggerEntityName; // 触发实体名称（用于显示）
    private Long triggerFieldId;
    private String triggerFieldName; // 触发字段名称（用于显示）
    private String triggerCondition;

    // 目标配置
    private Long targetEntityId;
    private String targetEntityName; // 目标实体名称（用于显示）
    private Long targetFieldId;
    private String targetFieldName; // 目标字段名称（用于显示）
    private String action; // required/readonly/hidden/setvalue
    private String actionValue;

    // 提示
    private String errorMessage;
    private String errorLevel;

    private String status;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
}
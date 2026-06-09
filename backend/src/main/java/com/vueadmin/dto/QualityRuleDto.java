package com.vueadmin.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 质量规则DTO
 */
@Data
public class QualityRuleDto {

    private Long id;
    private String ruleCode;
    private String ruleName;
    private String ruleType;
    private String checkType;
    private String checkConfig;
    private Long viewId;
    private String viewName;
    private Long entityId;
    private String entityName;
    private String entityType;
    private String tableName;
    private Long fieldId;
    private String fieldCode;
    private String fieldName;
    private String ruleConfig;
    private BigDecimal threshold;
    private String severity;
    private String status;
    private Long fieldStandardId;
    private Long valueDomainId;
    private Long templateId;
    private Boolean isScheduled;
    private String cronExpression;
    private LocalDateTime lastCheckTime;
    private BigDecimal lastPassRate;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
    private String description;
}

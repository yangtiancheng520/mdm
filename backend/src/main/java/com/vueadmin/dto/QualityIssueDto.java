package com.vueadmin.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 问题DTO
 */
@Data
public class QualityIssueDto {

    private Long id;
    private String issueCode;
    private Long taskId;
    private String taskName;
    private Long resultId;
    private Long ruleId;
    private String ruleName;
    private Long viewId;
    private String viewName;
    private Long entityId;
    private String entityName;
    private String entityType;
    private String tableName;
    private Long recordId;
    private Long mainRecordId;
    private String fieldCode;
    private String fieldName;
    private String fieldValue;
    private String issueType;
    private String issueLevel;
    private String issueDesc;
    private String status;
    private String assignee;
    private LocalDateTime dueDate;
    private String resolvedBy;
    private LocalDateTime resolvedTime;
    private String resolution;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

package com.vueadmin.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 检测结果DTO
 */
@Data
public class QualityCheckResultDto {

    private Long id;
    private Long taskId;
    private Long ruleId;
    private String ruleName;
    private Long entityId;
    private String entityName;
    private String entityType;
    private String tableName;
    private Long recordId;
    private Long mainRecordId;
    private String fieldCode;
    private String fieldName;
    private String fieldValue;
    private Integer isPassed;
    private String issueType;
    private String issueLevel;
    private String issueMessage;
    private LocalDateTime createdAt;
}

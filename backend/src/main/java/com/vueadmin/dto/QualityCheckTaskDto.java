package com.vueadmin.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 检测任务DTO
 */
@Data
public class QualityCheckTaskDto {

    private Long id;
    private String taskCode;
    private String taskName;
    private Long viewId;
    private String viewName;
    private String entityIds;
    private String ruleIds;
    private String status;
    private Integer totalRecords;
    private Integer totalRules;
    private Integer passCount;
    private Integer failCount;
    private BigDecimal passRate;
    private Integer mainTotal;
    private Integer mainPass;
    private Integer mainFail;
    private Integer subTotal;
    private Integer subPass;
    private Integer subFail;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer durationMs;
    private String errorMessage;
    private String createdBy;
    private LocalDateTime createdAt;

    // 指定检测的记录列表
    private List<CheckRecordDto> records;

    /**
     * 检测记录DTO
     */
    @Data
    public static class CheckRecordDto {
        private Long viewId;
        private String tableName;
        private Long recordId;
    }
}

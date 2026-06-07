package com.vueadmin.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 数据标准视图DTO
 */
@Data
public class DataStandardDto {

    private Long id;

    private String standardCode;

    private String standardName;

    private Long categoryId;

    private String categoryName;

    /**
     * 字段定义列表
     */
    private List<FieldDefinition> fieldsDefinition;

    private String status;

    private Integer version;

    private LocalDateTime publishTime;

    private String approvalStatus;

    private String approvalBy;

    private LocalDateTime approvalTime;

    private String approvalComment;

    private String createdBy;

    private LocalDateTime createdAt;

    private String updatedBy;

    private LocalDateTime updatedAt;

    private String description;

    /**
     * 字段定义内部类
     */
    @Data
    public static class FieldDefinition {
        private Long fieldId;
        private String fieldCode;
        private String fieldName;
        private Boolean required;
        private String defaultValue;
        private Integer sortOrder;
    }
}

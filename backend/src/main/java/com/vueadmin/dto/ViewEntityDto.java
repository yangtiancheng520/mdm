package com.vueadmin.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 实体定义DTO
 */
@Data
public class ViewEntityDto {

    private Long id;
    private Long viewId;
    private String entityCode;
    private String entityName;
    private String entityType; // main-主实体 sub-子实体
    private String tableName; // 物理表名

    // 子实体配置
    private Integer sort;
    private Integer minRows;
    private Integer maxRows;

    // 功能配置
    private Boolean enableAdd;
    private Boolean enableDelete;
    private Boolean enableCopy;
    private Boolean enableSort;
    private Boolean isInherited;  // 是否继承实体（修订时）

    private String status;
    private String description;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;

    // 字段分组列表
    private List<ViewGroupDto> groups;

    // 字段列表
    private List<ViewFieldDto> fields;
}

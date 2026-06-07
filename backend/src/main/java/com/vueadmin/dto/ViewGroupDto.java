package com.vueadmin.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 字段分组DTO
 */
@Data
public class ViewGroupDto {

    private Long id;
    private Long entityId;
    private String groupCode;
    private String groupName;
    private Integer sort;

    // 显示配置
    private Integer columnCount;
    private Boolean collapsible;
    private Boolean defaultCollapsed;

    private String status;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
}

package com.vueadmin.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 视图定义DTO（包含完整的视图结构）
 */
@Data
public class ViewDefinitionDto {

    private Long id;
    private String viewCode;
    private String viewName;
    private Long categoryId;
    private String categoryName;

    // 版本管理
    private Integer version;
    private Long baseVersionId;
    private Boolean isLatest;

    // 布局配置
    private Integer layoutColumns;
    private Integer labelWidth;

    // 功能开关
    private Boolean enableCopy;
    private Boolean enableImport;
    private Boolean enableExport;

    // 状态
    private String status;
    private LocalDateTime publishTime;

    private String description;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;

    // 实体列表（主实体 + 子实体）
    private List<ViewEntityDto> entities;

    // 校验规则列表
    private List<ViewValidationDto> validations;
}

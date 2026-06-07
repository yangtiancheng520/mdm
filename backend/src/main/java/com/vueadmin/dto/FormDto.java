package com.vueadmin.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 表单DTO
 */
@Data
public class FormDto {

    private Long id;
    private String formCode;
    private String formName;
    private String formType;
    private Long viewId;
    private String viewName;

    // 设计模式
    private String designMode;
    private String styleTemplate;

    // 布局配置
    private String layoutConfig;

    // 功能开关
    private Boolean enableCopy;
    private Boolean enableImport;
    private Boolean enableExport;

    // 状态
    private String status;
    private Boolean isDefault;
    private Integer version;

    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
    private String description;
}

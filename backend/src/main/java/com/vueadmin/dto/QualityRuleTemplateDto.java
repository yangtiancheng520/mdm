package com.vueadmin.dto;

import lombok.Data;

/**
 * 质量规则模板DTO
 */
@Data
public class QualityRuleTemplateDto {

    private Long id;
    private String templateCode;
    private String templateName;
    private String templateType;
    private String checkType;
    private String checkConfig;
    private String severity;
    private String description;
    private String status;

    /**
     * 是否系统模板
     */
    private Boolean isSystem;

    /**
     * 模板分类
     */
    private String category;

    /**
     * 标签
     */
    private String tags;

    private String createdBy;
    private String createdAt;
    private String updatedBy;
    private String updatedAt;
}

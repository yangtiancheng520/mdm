package com.vueadmin.dto;

import lombok.Data;

/**
 * 外部集成配置DTO
 */
@Data
public class IntegrationConfigDto {

    private Long id;

    private String integrationCode;

    private String integrationName;

    private String integrationType;

    private String apiEndpoint;

    private String authType;

    private String authConfig;

    private String requestConfig;

    private String mappingConfig;

    private String status;

    private String createdBy;

    private String createdAt;

    private String updatedBy;

    private String updatedAt;

    private String description;

    /**
     * 集成类型显示名称
     */
    private String integrationTypeName;

    /**
     * 认证类型显示名称
     */
    private String authTypeName;

    /**
     * 连接状态
     */
    private String connectionStatus;

    /**
     * 最后测试时间
     */
    private String lastTestTime;
}

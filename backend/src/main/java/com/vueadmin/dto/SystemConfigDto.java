package com.vueadmin.dto;

import lombok.Data;

/**
 * 系统配置DTO
 */
@Data
public class SystemConfigDto {

    private Long id;

    private String configKey;

    private String configValue;

    private String configType;

    private String configGroup;

    private String description;

    private Boolean isEncrypted;

    private String createdBy;

    private String createdAt;

    private String updatedBy;

    private String updatedAt;
}

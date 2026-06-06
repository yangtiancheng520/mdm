package com.vueadmin.dto;

import lombok.Data;
import java.util.List;

/**
 * 数据权限DTO
 */
@Data
public class DataPermissionDto {

    private Long id;

    private Long roleId;

    private String roleName;

    private String dataType;

    private String permissionType;

    private List<Long> orgIds;

    private String customRule;

    private String createdBy;

    private String createdAt;

    private String updatedBy;

    private String updatedAt;

    /**
     * 权限类型显示名称
     */
    private String permissionTypeName;

    /**
     * 数据类型显示名称
     */
    private String dataTypeName;
}

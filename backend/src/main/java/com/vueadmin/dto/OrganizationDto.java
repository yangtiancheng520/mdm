package com.vueadmin.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 组织机构DTO
 */
@Data
public class OrganizationDto {

    private Long id;

    private String orgCode;

    private String orgName;

    private String orgType;

    private Long parentId;

    private String parentName;

    private Integer level;

    private String path;

    private String manager;

    private String phone;

    private String email;

    private Integer sort;

    private String status;

    private String createdBy;

    private LocalDateTime createdAt;

    private String updatedBy;

    private LocalDateTime updatedAt;

    private String description;

    /**
     * 子组织列表（用于树形结构）
     */
    private List<OrganizationDto> children;

    /**
     * 是否有子节点
     */
    private Boolean hasChildren;

    /**
     * 节点标签（用于树形组件）
     */
    private String label;

    /**
     * 节点值（用于树形组件）
     */
    private Object value;
}

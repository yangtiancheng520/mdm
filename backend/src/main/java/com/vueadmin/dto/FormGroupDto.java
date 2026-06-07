package com.vueadmin.dto;

import lombok.Data;

/**
 * 表单分组DTO
 */
@Data
public class FormGroupDto {

    private Long id;
    private Long formId;
    private String groupCode;
    private String groupName;
    private String groupType; // group/tab/table
    private Integer sort;
    private Boolean isCollapsed;
}

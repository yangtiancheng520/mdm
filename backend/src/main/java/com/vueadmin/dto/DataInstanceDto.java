package com.vueadmin.dto;

import lombok.Data;

/**
 * 数据实例DTO
 */
@Data
public class DataInstanceDto {

    private Long id;
    private Long categoryId;
    private Long formId;
    private String formName;
    private Long viewId;
    private String dataJson;
    private String status;
    private String createdBy;
    private String createdAt;
    private String updatedBy;
    private String updatedAt;
}

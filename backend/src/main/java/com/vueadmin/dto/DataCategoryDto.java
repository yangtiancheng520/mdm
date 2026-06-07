package com.vueadmin.dto;

import lombok.Data;
import java.util.List;

/**
 * 数据分类DTO
 */
@Data
public class DataCategoryDto {

    private Long id;
    private Long parentId;
    private String name;
    private String type; // folder / form
    private Long formId;
    private String formName; // 表单名称
    private String icon;
    private Integer sort;

    // 子节点
    private List<DataCategoryDto> children;
}

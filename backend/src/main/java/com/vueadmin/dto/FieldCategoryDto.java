package com.vueadmin.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 字段分类DTO
 */
@Data
public class FieldCategoryDto {

    private Long id;

    /**
     * 分类编码
     */
    private String categoryCode;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 父级分类ID
     */
    private Long parentId;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态：active-启用/inactive-停用
     */
    private String status;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新人
     */
    private String updatedBy;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 描述
     */
    private String description;

    /**
     * 是否有子节点
     */
    private Boolean hasChildren;

    /**
     * 子分类列表
     */
    private List<FieldCategoryDto> children;

    /**
     * 树形选择器标签
     */
    private String label;

    /**
     * 树形选择器值
     */
    private Long value;
}

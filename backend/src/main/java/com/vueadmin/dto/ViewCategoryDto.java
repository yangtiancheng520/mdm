package com.vueadmin.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 视图分类DTO
 */
@Data
public class ViewCategoryDto {

    private Long id;

    private String categoryCode;

    private String categoryName;

    private Long parentId;

    private String parentName;

    private Integer sort;

    private String status;

    private String description;

    private String createdBy;

    private LocalDateTime createdAt;

    private String updatedBy;

    private LocalDateTime updatedAt;

    /**
     * 子分类列表
     */
    private List<ViewCategoryDto> children;

    /**
     * 是否有子分类
     */
    private Boolean hasChildren;

    /**
     * 分类下的视图数量
     */
    private Long viewCount;
}

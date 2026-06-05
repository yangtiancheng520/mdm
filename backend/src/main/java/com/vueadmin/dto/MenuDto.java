package com.vueadmin.dto;

import lombok.Data;

import java.util.List;

/**
 * 菜单树DTO
 */
@Data
public class MenuDto {

    private Long id;

    private String name;

    private String code;

    private String path;

    private String icon;

    private Integer sort;

    private String type;

    private Long parentId;

    /**
     * 子菜单
     */
    private List<MenuDto> children;
}

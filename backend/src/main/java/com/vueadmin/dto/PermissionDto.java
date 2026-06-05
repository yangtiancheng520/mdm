package com.vueadmin.dto;

import lombok.Data;
import java.util.List;

@Data
public class PermissionDto {
    private Long id;
    private String name;
    private String code;
    private String type;
    private Long parentId;
    private String path;
    private String icon;
    private Integer sort;
    private String status;
    private String createdAt;
    private List<PermissionDto> children;
}

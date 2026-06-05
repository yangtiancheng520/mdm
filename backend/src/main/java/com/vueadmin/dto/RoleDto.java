package com.vueadmin.dto;

import lombok.Data;
import java.util.List;

@Data
public class RoleDto {
    private Long id;
    private String name;
    private String code;
    private String description;
    private String status;
    private List<Long> permissions;
    private String createdAt;
}

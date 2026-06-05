package com.vueadmin.dto;

import lombok.Data;
import java.util.List;

@Data
public class UserDto {
    private Long id;
    private String account;
    private String password;
    private String name;
    private String email;
    private String phone;
    private String status;
    private List<Long> roles;
    private String createdAt;
}

package com.vueadmin.controller;

import com.vueadmin.dto.*;
import com.vueadmin.entity.Role;
import com.vueadmin.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/role")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping("/all")
    public ApiResponse<List<Role>> all() {
        return ApiResponse.success(roleService.getAllRoles());
    }

    @GetMapping("/list")
    public ApiResponse<PageResult<RoleDto>> list(@RequestParam(required = false) String name) {
        return ApiResponse.success(roleService.getRoleList(name));
    }

    @PostMapping("/create")
    public ApiResponse<RoleDto> create(@RequestBody RoleDto dto) {
        Role role = roleService.createRole(dto);
        RoleDto result = new RoleDto();
        result.setId(role.getId());
        result.setName(role.getName());
        return ApiResponse.success(result);
    }

    @PutMapping("/update/{id}")
    public ApiResponse<?> update(@PathVariable Long id, @RequestBody RoleDto dto) {
        roleService.updateRole(id, dto);
        return ApiResponse.success();
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<?> delete(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ApiResponse.success();
    }
}

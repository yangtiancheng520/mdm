package com.vueadmin.controller;

import com.vueadmin.dto.*;
import com.vueadmin.entity.Permission;
import com.vueadmin.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/permission")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping("/list")
    public ApiResponse<PageResult<PermissionDto>> list(@RequestParam(required = false) String name) {
        return ApiResponse.success(permissionService.getPermissionList(name));
    }

    @GetMapping("/tree")
    public ApiResponse<List<PermissionDto>> tree() {
        return ApiResponse.success(permissionService.getPermissionTree());
    }

    @PostMapping("/create")
    public ApiResponse<PermissionDto> create(@RequestBody PermissionDto dto) {
        Permission p = permissionService.createPermission(dto);
        PermissionDto result = new PermissionDto();
        result.setId(p.getId());
        result.setName(p.getName());
        return ApiResponse.success(result);
    }

    @PutMapping("/update/{id}")
    public ApiResponse<?> update(@PathVariable Long id, @RequestBody PermissionDto dto) {
        permissionService.updatePermission(id, dto);
        return ApiResponse.success();
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<?> delete(@PathVariable Long id) {
        permissionService.deletePermission(id);
        return ApiResponse.success();
    }

    @PutMapping("/sort")
    public ApiResponse<?> updateSort(@RequestBody List<Map<String, Object>> items) {
        permissionService.updatePermissionSort(items);
        return ApiResponse.success();
    }
}

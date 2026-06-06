package com.vueadmin.controller;

import com.vueadmin.dto.*;
import com.vueadmin.service.system.DataPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 数据权限控制器
 */
@RestController
@RequestMapping("/api/data-permission")
@RequiredArgsConstructor
public class DataPermissionController {

    private final DataPermissionService dataPermissionService;

    @GetMapping("/list")
    public ApiResponse<PageResult<DataPermissionDto>> list(
            @RequestParam(required = false) Long roleId,
            @RequestParam(required = false) String dataType) {
        return ApiResponse.success(dataPermissionService.getList(roleId, dataType));
    }

    @GetMapping("/role/{roleId}")
    public ApiResponse<List<DataPermissionDto>> getByRoleId(@PathVariable Long roleId) {
        return ApiResponse.success(dataPermissionService.getByRoleId(roleId));
    }

    @GetMapping("/{id}")
    public ApiResponse<DataPermissionDto> getById(@PathVariable Long id) {
        return ApiResponse.success(dataPermissionService.getById(id));
    }

    @PostMapping("/create")
    public ApiResponse<DataPermissionDto> create(@RequestBody DataPermissionDto dto) {
        DataPermissionDto result = new DataPermissionDto();
        com.vueadmin.entity.system.DataPermission permission = dataPermissionService.create(dto);
        result.setId(permission.getId());
        return ApiResponse.success(result);
    }

    @PutMapping("/update/{id}")
    public ApiResponse<?> update(@PathVariable Long id, @RequestBody DataPermissionDto dto) {
        dataPermissionService.update(id, dto);
        return ApiResponse.success();
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<?> delete(@PathVariable Long id) {
        dataPermissionService.delete(id);
        return ApiResponse.success();
    }

    @PostMapping("/role/{roleId}/save")
    public ApiResponse<?> saveRolePermissions(
            @PathVariable Long roleId,
            @RequestBody List<DataPermissionDto> permissions) {
        dataPermissionService.saveRolePermissions(roleId, permissions);
        return ApiResponse.success();
    }
}

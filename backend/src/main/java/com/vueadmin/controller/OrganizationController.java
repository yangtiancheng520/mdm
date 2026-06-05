package com.vueadmin.controller;

import com.vueadmin.dto.ApiResponse;
import com.vueadmin.dto.OrganizationDto;
import com.vueadmin.service.system.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 组织机构控制器
 */
@Tag(name = "组织管理", description = "组织机构管理接口")
@RestController
@RequestMapping("/api/organization")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    /**
     * 获取组织树
     */
    @Operation(summary = "获取组织树", description = "获取完整的组织树形结构")
    @GetMapping("/tree")
    public ApiResponse<List<OrganizationDto>> getTree() {
        return ApiResponse.success(organizationService.getOrganizationTree());
    }

    /**
     * 获取启用的组织树
     */
    @Operation(summary = "获取启用的组织树")
    @GetMapping("/tree/active")
    public ApiResponse<List<OrganizationDto>> getActiveTree() {
        return ApiResponse.success(organizationService.getActiveOrganizationTree());
    }

    /**
     * 获取组织列表
     */
    @Operation(summary = "获取组织列表")
    @GetMapping("/list")
    public ApiResponse<List<OrganizationDto>> getList() {
        return ApiResponse.success(organizationService.getOrganizationList());
    }

    /**
     * 获取组织详情
     */
    @Operation(summary = "获取组织详情")
    @GetMapping("/{id}")
    public ApiResponse<OrganizationDto> getById(
            @Parameter(description = "组织ID") @PathVariable Long id) {
        return ApiResponse.success(organizationService.getById(id));
    }

    /**
     * 获取子组织
     */
    @Operation(summary = "获取子组织")
    @GetMapping("/{id}/children")
    public ApiResponse<List<OrganizationDto>> getChildren(
            @Parameter(description = "父组织ID") @PathVariable Long id) {
        return ApiResponse.success(organizationService.getChildren(id));
    }

    /**
     * 创建组织
     */
    @Operation(summary = "创建组织")
    @PostMapping
    public ApiResponse<OrganizationDto> create(@Valid @RequestBody OrganizationDto dto) {
        return ApiResponse.success(organizationService.create(dto));
    }

    /**
     * 更新组织
     */
    @Operation(summary = "更新组织")
    @PutMapping("/{id}")
    public ApiResponse<OrganizationDto> update(
            @Parameter(description = "组织ID") @PathVariable Long id,
            @Valid @RequestBody OrganizationDto dto) {
        return ApiResponse.success(organizationService.update(id, dto));
    }

    /**
     * 删除组织
     */
    @Operation(summary = "删除组织")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(
            @Parameter(description = "组织ID") @PathVariable Long id) {
        organizationService.delete(id);
        return ApiResponse.success();
    }

    /**
     * 批量删除
     */
    @Operation(summary = "批量删除组织")
    @DeleteMapping("/batch")
    public ApiResponse<Void> batchDelete(@RequestBody List<Long> ids) {
        organizationService.batchDelete(ids);
        return ApiResponse.success();
    }
}

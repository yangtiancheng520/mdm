package com.vueadmin.controller.standard;

import com.vueadmin.dto.ApiResponse;
import com.vueadmin.dto.FieldCategoryDto;
import com.vueadmin.service.standard.FieldCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字段分类Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/standard/field-category")
@RequiredArgsConstructor
public class FieldCategoryController {

    private final FieldCategoryService fieldCategoryService;

    /**
     * 获取分类树
     */
    @GetMapping("/tree")
    public ApiResponse<List<FieldCategoryDto>> getTree() {
        List<FieldCategoryDto> tree = fieldCategoryService.getCategoryTree();
        return ApiResponse.success(tree);
    }

    /**
     * 获取启用的分类树
     */
    @GetMapping("/active-tree")
    public ApiResponse<List<FieldCategoryDto>> getActiveTree() {
        List<FieldCategoryDto> tree = fieldCategoryService.getActiveCategoryTree();
        return ApiResponse.success(tree);
    }

    /**
     * 获取分类列表（平铺）
     */
    @GetMapping("/list")
    public ApiResponse<List<FieldCategoryDto>> getList() {
        List<FieldCategoryDto> list = fieldCategoryService.getCategoryList();
        return ApiResponse.success(list);
    }

    /**
     * 获取子分类
     */
    @GetMapping("/children/{parentId}")
    public ApiResponse<List<FieldCategoryDto>> getChildren(@PathVariable Long parentId) {
        List<FieldCategoryDto> children = fieldCategoryService.getChildren(parentId);
        return ApiResponse.success(children);
    }

    /**
     * 获取分类下的字段数量
     */
    @GetMapping("/field-count/{id}")
    public ApiResponse<Long> getFieldCount(@PathVariable Long id) {
        long count = fieldCategoryService.getFieldCount(id);
        return ApiResponse.success(count);
    }

    /**
     * 根据ID获取分类详情
     */
    @GetMapping("/{id}")
    public ApiResponse<FieldCategoryDto> getById(@PathVariable Long id) {
        FieldCategoryDto dto = fieldCategoryService.getById(id);
        return ApiResponse.success(dto);
    }

    /**
     * 创建分类
     */
    @PostMapping
    public ApiResponse<FieldCategoryDto> create(@RequestBody FieldCategoryDto dto) {
        FieldCategoryDto created = fieldCategoryService.create(dto);
        return ApiResponse.success(created);
    }

    /**
     * 更新分类
     */
    @PutMapping("/{id}")
    public ApiResponse<FieldCategoryDto> update(@PathVariable Long id, @RequestBody FieldCategoryDto dto) {
        FieldCategoryDto updated = fieldCategoryService.update(id, dto);
        return ApiResponse.success(updated);
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        fieldCategoryService.delete(id);
        return ApiResponse.success();
    }

    /**
     * 批量删除分类
     */
    @PostMapping("/batch-delete")
    public ApiResponse<Void> batchDelete(@RequestBody List<Long> ids) {
        fieldCategoryService.batchDelete(ids);
        return ApiResponse.success();
    }
}

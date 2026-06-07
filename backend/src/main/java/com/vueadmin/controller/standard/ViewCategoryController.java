package com.vueadmin.controller.standard;

import com.vueadmin.dto.ApiResponse;
import com.vueadmin.dto.ViewCategoryDto;
import com.vueadmin.service.standard.ViewCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 视图分类Controller
 */
@RestController
@RequestMapping("/api/view-category")
@Tag(name = "视图分类管理", description = "视图分类的增删改查接口")
public class ViewCategoryController {

    @Autowired
    private ViewCategoryService viewCategoryService;

    /**
     * 获取分类树
     */
    @GetMapping("/tree")
    @Operation(summary = "获取分类树", description = "获取完整的分类树结构")
    public ApiResponse<List<ViewCategoryDto>> getTree() {
        List<ViewCategoryDto> tree = viewCategoryService.getTree();
        return ApiResponse.success(tree);
    }

    /**
     * 获取启用的分类树
     */
    @GetMapping("/active-tree")
    @Operation(summary = "获取启用的分类树", description = "获取启用状态的分类树结构")
    public ApiResponse<List<ViewCategoryDto>> getActiveTree() {
        List<ViewCategoryDto> tree = viewCategoryService.getActiveTree();
        return ApiResponse.success(tree);
    }

    /**
     * 获取所有分类（扁平列表）
     */
    @GetMapping
    @Operation(summary = "获取所有分类", description = "获取所有分类的扁平列表")
    public ApiResponse<List<ViewCategoryDto>> getAll() {
        List<ViewCategoryDto> list = viewCategoryService.getAll();
        return ApiResponse.success(list);
    }

    /**
     * 根据ID查询
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询", description = "根据ID获取分类详情")
    public ApiResponse<ViewCategoryDto> getById(
            @Parameter(description = "分类ID")
            @PathVariable Long id) {
        ViewCategoryDto dto = viewCategoryService.findById(id);
        return ApiResponse.success(dto);
    }

    /**
     * 创建分类
     */
    @PostMapping
    @Operation(summary = "创建分类", description = "创建新的视图分类")
    public ApiResponse<ViewCategoryDto> create(
            @Parameter(description = "分类信息")
            @RequestBody ViewCategoryDto dto) {
        try {
            ViewCategoryDto result = viewCategoryService.create(dto);
            return ApiResponse.success("创建成功", result);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    /**
     * 更新分类
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新分类", description = "更新指定ID的分类")
    public ApiResponse<ViewCategoryDto> update(
            @Parameter(description = "分类ID")
            @PathVariable Long id,
            @Parameter(description = "分类信息")
            @RequestBody ViewCategoryDto dto) {
        try {
            ViewCategoryDto result = viewCategoryService.update(id, dto);
            return ApiResponse.success("更新成功", result);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除分类", description = "删除指定ID的分类")
    public ApiResponse<?> delete(
            @Parameter(description = "分类ID")
            @PathVariable Long id) {
        try {
            viewCategoryService.delete(id);
            return ApiResponse.success("删除成功");
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    /**
     * 获取分类下的视图数量
     */
    @GetMapping("/{id}/view-count")
    @Operation(summary = "获取视图数量", description = "获取分类下的视图数量")
    public ApiResponse<Long> getViewCount(
            @Parameter(description = "分类ID")
            @PathVariable Long id) {
        long count = viewCategoryService.getViewCount(id);
        return ApiResponse.success(count);
    }
}

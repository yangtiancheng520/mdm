package com.vueadmin.controller.data;

import com.vueadmin.dto.ApiResponse;
import com.vueadmin.dto.DataCategoryDto;
import com.vueadmin.service.data.DataCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 数据分类Controller
 */
@RestController
@RequestMapping("/api/data-category")
@RequiredArgsConstructor
public class DataCategoryController {

    private final DataCategoryService categoryService;

    /**
     * 获取分类树
     */
    @GetMapping("/tree")
    public ApiResponse<List<DataCategoryDto>> getTree() {
        List<DataCategoryDto> tree = categoryService.getCategoryTree();
        return ApiResponse.success(tree);
    }

    /**
     * 创建文件夹
     */
    @PostMapping("/folder")
    public ApiResponse<DataCategoryDto> createFolder(@RequestBody Map<String, Object> request) {
        Long parentId = request.get("parentId") != null ? Long.valueOf(request.get("parentId").toString()) : null;
        String name = (String) request.get("name");
        String icon = (String) request.get("icon");

        DataCategoryDto dto = categoryService.createFolder(parentId, name, icon);
        return ApiResponse.success(dto);
    }

    /**
     * 添加表单
     */
    @PostMapping("/form")
    public ApiResponse<DataCategoryDto> addForm(@RequestBody Map<String, Object> request) {
        Long parentId = request.get("parentId") != null ? Long.valueOf(request.get("parentId").toString()) : null;
        Long formId = Long.valueOf(request.get("formId").toString());

        DataCategoryDto dto = categoryService.addForm(parentId, formId);
        return ApiResponse.success(dto);
    }

    /**
     * 更新分类
     */
    @PutMapping("/{id}")
    public ApiResponse<DataCategoryDto> update(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        String name = (String) request.get("name");
        String icon = (String) request.get("icon");

        DataCategoryDto dto = categoryService.updateCategory(id, name, icon);
        return ApiResponse.success(dto);
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ApiResponse.success();
    }

    /**
     * 更新排序
     */
    @PutMapping("/sort")
    public ApiResponse<Void> updateSort(@RequestBody List<Map<String, Object>> items) {
        categoryService.updateSort(items);
        return ApiResponse.success();
    }
}
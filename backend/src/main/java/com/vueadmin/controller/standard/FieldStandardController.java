package com.vueadmin.controller.standard;

import com.vueadmin.dto.ApiResponse;
import com.vueadmin.dto.FieldStandardDto;
import com.vueadmin.dto.PageResult;
import com.vueadmin.service.standard.FieldStandardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 字段标准控制器
 * 提供字段标准的REST API接口
 */
@RestController
@RequestMapping("/api/field-standard")
@RequiredArgsConstructor
@Tag(name = "字段标准管理", description = "字段标准的增删改查及发布归档等操作")
public class FieldStandardController {

    private final FieldStandardService fieldStandardService;

    /**
     * 分页查询字段标准列表
     *
     * @param fieldCode  字段编码（模糊查询）
     * @param fieldName  字段名称（模糊查询）
     * @param fieldType  字段类型
     * @param status     状态
     * @param category   分类（旧字段，兼容）
     * @param categoryId 分类ID
     * @param page       页码
     * @param pageSize   每页大小
     * @return 分页结果
     */
    @GetMapping("/list")
    @Operation(summary = "分页查询字段标准列表", description = "支持按字段编码、名称、类型、状态、分类筛选")
    public ApiResponse<PageResult<FieldStandardDto>> list(
            @Parameter(description = "字段编码（模糊查询）")
            @RequestParam(required = false) String fieldCode,
            @Parameter(description = "字段名称（模糊查询）")
            @RequestParam(required = false) String fieldName,
            @Parameter(description = "字段类型")
            @RequestParam(required = false) String fieldType,
            @Parameter(description = "状态（draft/published/archived）")
            @RequestParam(required = false) String status,
            @Parameter(description = "分类（旧字段，兼容）")
            @RequestParam(required = false) String category,
            @Parameter(description = "分类ID")
            @RequestParam(required = false) Long categoryId,
            @Parameter(description = "页码，默认1")
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @Parameter(description = "每页大小，默认10")
            @RequestParam(required = false, defaultValue = "10") Integer pageSize) {

        PageResult<FieldStandardDto> result = fieldStandardService.search(
                fieldCode, fieldName, fieldType, status, category, categoryId, page, pageSize);
        return ApiResponse.success(result);
    }

    /**
     * 获取所有字段标准列表（不分页）
     *
     * @return 字段标准列表
     */
    @GetMapping("/all")
    @Operation(summary = "获取所有字段标准列表", description = "获取所有字段标准，不分页")
    public ApiResponse<List<FieldStandardDto>> getAll() {
        return ApiResponse.success(fieldStandardService.getAll());
    }

    /**
     * 获取已发布的字段标准列表
     *
     * @return 已启用的字段标准列表
     */
    @GetMapping("/active")
    @Operation(summary = "获取已启用的字段标准列表", description = "获取所有已启用状态的字段标准")
    public ApiResponse<List<FieldStandardDto>> getActive() {
        return ApiResponse.success(fieldStandardService.getActive());
    }

    /**
     * 根据状态获取字段标准列表
     *
     * @param status 状态
     * @return 字段标准列表
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "按状态获取字段标准列表", description = "根据状态筛选字段标准")
    public ApiResponse<List<FieldStandardDto>> getByStatus(
            @Parameter(description = "状态（draft/published/archived）")
            @PathVariable String status) {
        return ApiResponse.success(fieldStandardService.getByStatus(status));
    }

    /**
     * 根据分类获取字段标准列表
     *
     * @param category 分类
     * @return 字段标准列表
     */
    @GetMapping("/category/{category}")
    @Operation(summary = "按分类获取字段标准列表", description = "根据分类筛选字段标准")
    public ApiResponse<List<FieldStandardDto>> getByCategory(
            @Parameter(description = "分类")
            @PathVariable String category) {
        return ApiResponse.success(fieldStandardService.getByCategory(category));
    }

    /**
     * 获取所有分类列表
     *
     * @return 分类列表
     */
    @GetMapping("/categories")
    @Operation(summary = "获取所有分类列表", description = "获取字段标准的所有分类")
    public ApiResponse<List<String>> getAllCategories() {
        return ApiResponse.success(fieldStandardService.getAllCategories());
    }

    /**
     * 根据ID获取字段标准详情
     *
     * @param id 主键ID
     * @return 字段标准详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取字段标准详情", description = "根据ID获取字段标准详细信息")
    public ApiResponse<FieldStandardDto> getById(
            @Parameter(description = "字段标准ID")
            @PathVariable Long id) {
        FieldStandardDto dto = fieldStandardService.getById(id);
        if (dto == null) {
            return ApiResponse.error(404, "字段标准不存在");
        }
        return ApiResponse.success(dto);
    }

    /**
     * 根据字段编码获取字段标准详情
     *
     * @param fieldCode 字段编码
     * @return 字段标准详情
     */
    @GetMapping("/code/{fieldCode}")
    @Operation(summary = "根据字段编码获取详情", description = "根据字段编码获取字段标准详细信息")
    public ApiResponse<FieldStandardDto> getByFieldCode(
            @Parameter(description = "字段编码")
            @PathVariable String fieldCode) {
        FieldStandardDto dto = fieldStandardService.getByFieldCode(fieldCode);
        if (dto == null) {
            return ApiResponse.error(404, "字段标准不存在");
        }
        return ApiResponse.success(dto);
    }

    /**
     * 创建字段标准
     *
     * @param dto 字段标准DTO
     * @return 创建后的字段标准
     */
    @PostMapping
    @Operation(summary = "创建字段标准", description = "创建新的字段标准，初始状态为草稿")
    public ApiResponse<FieldStandardDto> create(
            @Parameter(description = "字段标准信息")
            @RequestBody FieldStandardDto dto) {
        try {
            FieldStandardDto result = fieldStandardService.create(dto);
            return ApiResponse.success("创建成功", result);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    /**
     * 更新字段标准
     *
     * @param id  主键ID
     * @param dto 字段标准DTO
     * @return 更新后的字段标准
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新字段标准", description = "更新指定ID的字段标准信息")
    public ApiResponse<FieldStandardDto> update(
            @Parameter(description = "字段标准ID")
            @PathVariable Long id,
            @Parameter(description = "字段标准信息")
            @RequestBody FieldStandardDto dto) {
        try {
            FieldStandardDto result = fieldStandardService.update(id, dto);
            return ApiResponse.success("更新成功", result);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    /**
     * 批量删除字段标准
     *
     * @param params 包含ids的参数
     * @return 操作结果
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除字段标准", description = "批量删除多个字段标准，已发布的字段标准不能删除")
    public ApiResponse<?> batchDelete(
            @Parameter(description = "批量删除参数，包含ids数组")
            @RequestBody Map<String, Object> params) {
        try {
            @SuppressWarnings("unchecked")
            List<Integer> idInts = (List<Integer>) params.get("ids");
            String updatedBy = (String) params.get("updatedBy");

            List<Long> ids = idInts.stream()
                    .map(Integer::longValue)
                    .collect(java.util.stream.Collectors.toList());

            fieldStandardService.batchDelete(ids, updatedBy);
            return ApiResponse.success("批量删除成功");
        } catch (Exception e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    /**
     * 删除字段标准
     *
     * @param id 主键ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除字段标准", description = "删除指定ID的字段标准，已发布的字段标准不能删除")
    public ApiResponse<?> delete(
            @Parameter(description = "字段标准ID")
            @PathVariable Long id) {
        try {
            fieldStandardService.delete(id);
            return ApiResponse.success("删除成功");
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    /**
     * 启用字段标准
     *
     * @param id 主键ID
     * @return 更新后的字段标准
     */
    @PutMapping("/{id}/activate")
    @Operation(summary = "启用字段标准", description = "启用字段标准")
    public ApiResponse<FieldStandardDto> activate(
            @Parameter(description = "字段标准ID")
            @PathVariable Long id) {
        try {
            FieldStandardDto result = fieldStandardService.activate(id);
            return ApiResponse.success("启用成功", result);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    /**
     * 停用字段标准
     *
     * @param id 主键ID
     * @return 更新后的字段标准
     */
    @PutMapping("/{id}/deactivate")
    @Operation(summary = "停用字段标准", description = "停用字段标准")
    public ApiResponse<FieldStandardDto> deactivate(
            @Parameter(description = "字段标准ID")
            @PathVariable Long id) {
        try {
            FieldStandardDto result = fieldStandardService.deactivate(id);
            return ApiResponse.success("停用成功", result);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    /**
     * 批量更新分类
     *
     * @param params 包含ids和category的参数
     * @return 操作结果
     */
    @PostMapping("/batch/category")
    @Operation(summary = "批量更新分类", description = "批量更新多个字段标准的分类")
    public ApiResponse<?> batchUpdateCategory(
            @Parameter(description = "批量更新参数")
            @RequestBody Map<String, Object> params) {
        try {
            @SuppressWarnings("unchecked")
            List<Integer> idInts = (List<Integer>) params.get("ids");
            String category = (String) params.get("category");
            String updatedBy = (String) params.get("updatedBy");

            List<Long> ids = idInts.stream()
                    .map(Integer::longValue)
                    .collect(java.util.stream.Collectors.toList());

            fieldStandardService.batchUpdateCategory(ids, category, updatedBy);
            return ApiResponse.success("批量更新成功");
        } catch (Exception e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    /**
     * 获取字段标准统计信息
     *
     * @return 统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取统计信息", description = "获取各状态的字段标准数量统计")
    public ApiResponse<Map<String, Long>> getStatistics() {
        Map<String, Long> statistics = new java.util.HashMap<>();
        statistics.put("启用", fieldStandardService.countByStatus("启用"));
        statistics.put("停用", fieldStandardService.countByStatus("停用"));
        return ApiResponse.success(statistics);
    }
}

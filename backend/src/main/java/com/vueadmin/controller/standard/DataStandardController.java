package com.vueadmin.controller.standard;

import com.vueadmin.dto.ApiResponse;
import com.vueadmin.dto.DataStandardDto;
import com.vueadmin.service.standard.DataStandardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 数据标准视图Controller
 */
@RestController
@RequestMapping("/api/data-standard")
@Tag(name = "数据标准视图管理", description = "数据标准视图的增删改查接口")
public class DataStandardController {

    @Autowired
    private DataStandardService dataStandardService;

    /**
     * 分页查询数据标准
     */
    @GetMapping
    @Operation(summary = "分页查询", description = "分页查询数据标准列表")
    public ApiResponse<Page<DataStandardDto>> search(
            @Parameter(description = "关键词")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "状态")
            @RequestParam(required = false) String status,
            @Parameter(description = "分类ID")
            @RequestParam(required = false) Long categoryId,
            @Parameter(description = "页码")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "排序字段")
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @Parameter(description = "排序方向")
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = "asc".equalsIgnoreCase(sortDir)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<DataStandardDto> result = dataStandardService.search(
                keyword, status, categoryId, pageable);
        return ApiResponse.success(result);
    }

    /**
     * 根据ID查询
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询", description = "根据ID获取数据标准详情")
    public ApiResponse<DataStandardDto> findById(
            @Parameter(description = "数据标准ID")
            @PathVariable Long id) {
        DataStandardDto result = dataStandardService.findById(id);
        return ApiResponse.success(result);
    }

    /**
     * 创建数据标准
     */
    @PostMapping
    @Operation(summary = "创建数据标准", description = "创建新的数据标准")
    public ApiResponse<DataStandardDto> create(
            @Parameter(description = "数据标准信息")
            @RequestBody DataStandardDto dto) {
        try {
            DataStandardDto result = dataStandardService.create(dto);
            return ApiResponse.success("创建成功", result);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    /**
     * 更新数据标准
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新数据标准", description = "更新指定ID的数据标准")
    public ApiResponse<DataStandardDto> update(
            @Parameter(description = "数据标准ID")
            @PathVariable Long id,
            @Parameter(description = "数据标准信息")
            @RequestBody DataStandardDto dto) {
        try {
            DataStandardDto result = dataStandardService.update(id, dto);
            return ApiResponse.success("更新成功", result);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    /**
     * 删除数据标准
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除数据标准", description = "删除指定ID的数据标准")
    public ApiResponse<?> delete(
            @Parameter(description = "数据标准ID")
            @PathVariable Long id) {
        try {
            dataStandardService.delete(id);
            return ApiResponse.success("删除成功");
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    /**
     * 批量删除
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除", description = "批量删除多个数据标准")
    public ApiResponse<?> batchDelete(
            @Parameter(description = "批量删除参数")
            @RequestBody Map<String, Object> params) {
        try {
            @SuppressWarnings("unchecked")
            List<Integer> idInts = (List<Integer>) params.get("ids");
            String updatedBy = (String) params.get("updatedBy");

            List<Long> ids = idInts.stream()
                    .map(Integer::longValue)
                    .collect(java.util.stream.Collectors.toList());

            dataStandardService.batchDelete(ids, updatedBy);
            return ApiResponse.success("批量删除成功");
        } catch (Exception e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    /**
     * 发布数据标准
     */
    @PutMapping("/{id}/publish")
    @Operation(summary = "发布数据标准", description = "发布数据标准")
    public ApiResponse<DataStandardDto> publish(
            @Parameter(description = "数据标准ID")
            @PathVariable Long id) {
        try {
            DataStandardDto result = dataStandardService.publish(id);
            return ApiResponse.success("发布成功", result);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    /**
     * 归档数据标准
     */
    @PutMapping("/{id}/archive")
    @Operation(summary = "归档数据标准", description = "归档数据标准")
    public ApiResponse<DataStandardDto> archive(
            @Parameter(description = "数据标准ID")
            @PathVariable Long id) {
        try {
            DataStandardDto result = dataStandardService.archive(id);
            return ApiResponse.success("归档成功", result);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    /**
     * 审批数据标准
     */
    @PutMapping("/{id}/approve")
    @Operation(summary = "审批数据标准", description = "审批数据标准")
    public ApiResponse<DataStandardDto> approve(
            @Parameter(description = "数据标准ID")
            @PathVariable Long id,
            @Parameter(description = "审批参数")
            @RequestBody Map<String, Object> params) {
        try {
            String approvalBy = (String) params.get("approvalBy");
            boolean approved = Boolean.TRUE.equals(params.get("approved"));
            String comment = (String) params.get("comment");

            DataStandardDto result = dataStandardService.approve(id, approvalBy, approved, comment);
            return ApiResponse.success(approved ? "审批通过" : "审批驳回", result);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    /**
     * 获取统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取统计信息", description = "获取各状态的数据标准数量统计")
    public ApiResponse<Map<String, Long>> getStatistics() {
        Map<String, Long> statistics = new java.util.HashMap<>();
        statistics.put("draft", dataStandardService.countByStatus("draft"));
        statistics.put("published", dataStandardService.countByStatus("published"));
        statistics.put("archived", dataStandardService.countByStatus("archived"));
        return ApiResponse.success(statistics);
    }
}

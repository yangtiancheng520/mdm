package com.vueadmin.controller.quality;

import com.vueadmin.dto.ApiResponse;
import com.vueadmin.dto.QualityCheckTaskDto;
import com.vueadmin.dto.QualityCheckResultDto;
import com.vueadmin.service.quality.QualityCheckService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 质量检测Controller
 */
@RestController
@RequestMapping("/api/quality/check")
@RequiredArgsConstructor
@Tag(name = "质量检测管理", description = "质量检测任务的创建和执行")
public class QualityCheckController {

    private final QualityCheckService qualityCheckService;

    /**
     * 获取检测任务列表
     */
    @GetMapping("/task/list")
    @Operation(summary = "获取检测任务列表")
    public ApiResponse<List<QualityCheckTaskDto>> getTaskList(
            @RequestParam(required = false) Long viewId,
            @RequestParam(required = false) String status) {
        List<QualityCheckTaskDto> tasks = qualityCheckService.getTaskList(viewId, status);
        return ApiResponse.success(tasks);
    }

    /**
     * 获取任务详情
     */
    @GetMapping("/task/{id}")
    @Operation(summary = "获取任务详情")
    public ApiResponse<QualityCheckTaskDto> getTaskById(@PathVariable Long id) {
        QualityCheckTaskDto task = qualityCheckService.getTaskById(id);
        return ApiResponse.success(task);
    }

    /**
     * 执行检测任务
     */
    @PostMapping("/execute")
    @Operation(summary = "执行检测任务")
    public ApiResponse<QualityCheckTaskDto> executeCheck(@RequestBody QualityCheckTaskDto dto) {
        String currentUser = getCurrentUser();
        QualityCheckTaskDto task = qualityCheckService.executeCheck(dto, currentUser);
        return ApiResponse.success(task);
    }

    /**
     * 获取检测结果
     */
    @GetMapping("/result/{taskId}")
    @Operation(summary = "获取检测结果")
    public ApiResponse<List<QualityCheckResultDto>> getResults(@PathVariable Long taskId) {
        List<QualityCheckResultDto> results = qualityCheckService.getResultsByTaskId(taskId);
        return ApiResponse.success(results);
    }

    /**
     * 获取检测失败的结果
     */
    @GetMapping("/result/{taskId}/failed")
    @Operation(summary = "获取检测失败的结果")
    public ApiResponse<List<QualityCheckResultDto>> getFailedResults(@PathVariable Long taskId) {
        List<QualityCheckResultDto> results = qualityCheckService.getFailedResults(taskId);
        return ApiResponse.success(results);
    }

    /**
     * 删除检测任务
     */
    @DeleteMapping("/task/{id}")
    @Operation(summary = "删除检测任务")
    public ApiResponse<Void> deleteTask(@PathVariable Long id) {
        qualityCheckService.deleteTask(id);
        return ApiResponse.success(null);
    }

    /**
     * 获取待确认记录列表
     */
    @GetMapping("/task/{taskId}/pending-records")
    @Operation(summary = "获取待确认记录列表")
    public ApiResponse<List<Map<String, Object>>> getPendingRecords(@PathVariable Long taskId) {
        List<Map<String, Object>> records = qualityCheckService.getPendingRecords(taskId);
        return ApiResponse.success(records);
    }

    /**
     * 确认记录状态
     */
    @PostMapping("/confirm")
    @Operation(summary = "确认记录状态")
    public ApiResponse<Void> confirmRecords(@RequestBody Map<String, Object> request) {
        String currentUser = getCurrentUser();
        qualityCheckService.confirmRecords(request, currentUser);
        return ApiResponse.success(null);
    }

    /**
     * 获取当前用户
     */
    private String getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "system";
    }
}

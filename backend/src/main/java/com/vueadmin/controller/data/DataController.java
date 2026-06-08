package com.vueadmin.controller.data;

import com.vueadmin.dto.ApiResponse;
import com.vueadmin.dto.DataOperationLogDto;
import com.vueadmin.service.data.DataOperationLogService;
import com.vueadmin.service.data.PhysicalTableDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 数据维护Controller
 * 负责从物理表读写数据
 */
@RestController
@RequestMapping("/api/data")
@RequiredArgsConstructor
public class DataController {

    private final PhysicalTableDataService physicalTableDataService;
    private final DataOperationLogService operationLogService;

    /**
     * 查询数据列表（从物理表）
     */
    @GetMapping("/list")
    public ApiResponse<List<Map<String, Object>>> getList(
            @RequestParam(required = false) Long categoryId,
            @RequestParam Long formId,
            @RequestParam(required = false) String status) {
        List<Map<String, Object>> list = physicalTableDataService.getDataList(formId, status);
        return ApiResponse.success(list);
    }

    /**
     * 获取数据详情（从物理表）
     */
    @GetMapping("/{formId}/{recordId}")
    public ApiResponse<Map<String, Object>> getById(
            @PathVariable Long formId,
            @PathVariable Long recordId) {
        Map<String, Object> data = physicalTableDataService.getDataById(formId, recordId);
        return ApiResponse.success(data);
    }

    /**
     * 保存数据（到物理表）
     */
    @PostMapping
    public ApiResponse<Long> save(@RequestBody Map<String, Object> request) {
        Long categoryId = Long.valueOf(request.get("categoryId").toString());
        Long formId = Long.valueOf(request.get("formId").toString());
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) request.get("data");

        String currentUser = getCurrentUser();
        Long recordId = physicalTableDataService.saveData(categoryId, formId, data, currentUser);

        return ApiResponse.success(recordId);
    }

    /**
     * 更新数据（到物理表）
     */
    @PutMapping("/{categoryId}/{formId}/{recordId}")
    public ApiResponse<Void> update(
            @PathVariable Long categoryId,
            @PathVariable Long formId,
            @PathVariable Long recordId,
            @RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) request.get("data");

        String currentUser = getCurrentUser();
        physicalTableDataService.updateData(categoryId, formId, recordId, data, currentUser);

        return ApiResponse.success();
    }

    /**
     * 删除数据（软删除）
     */
    @DeleteMapping("/{categoryId}/{formId}/{recordId}")
    public ApiResponse<Void> delete(
            @PathVariable Long categoryId,
            @PathVariable Long formId,
            @PathVariable Long recordId) {
        String currentUser = getCurrentUser();
        physicalTableDataService.deleteData(categoryId, formId, recordId, currentUser);

        return ApiResponse.success();
    }

    /**
     * 查询操作日志
     */
    @GetMapping("/logs")
    public ApiResponse<List<DataOperationLogDto>> getLogs(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long formId) {
        List<DataOperationLogDto> logs = operationLogService.getList(categoryId, formId);
        return ApiResponse.success(logs);
    }

    /**
     * 获取当前用户
     */
    private String getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "system";
    }
}

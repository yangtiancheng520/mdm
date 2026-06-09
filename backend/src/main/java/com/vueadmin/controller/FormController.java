package com.vueadmin.controller;

import com.vueadmin.dto.ApiResponse;
import com.vueadmin.dto.FormDesignRequest;
import com.vueadmin.dto.FormDto;
import com.vueadmin.dto.FormLogDto;
import com.vueadmin.service.form.FormLogService;
import com.vueadmin.service.form.FormService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/form")
@RequiredArgsConstructor
@Slf4j
public class FormController {

    private final FormService formService;
    private final FormLogService formLogService;
    private final EntityManager entityManager;

    /**
     * 获取表单列表
     */
    @GetMapping("/list")
    public ApiResponse<List<FormDto>> getFormList(
            @RequestParam(required = false) String formName,
            @RequestParam(required = false) String formType,
            @RequestParam(required = false) Long viewId,
            @RequestParam(required = false) String status) {
        List<FormDto> list = formService.getFormList(formName, formType, viewId, status);
        return ApiResponse.success(list);
    }

    /**
     * 获取表单详情
     */
    @GetMapping("/{id}")
    public ApiResponse<FormDto> getForm(@PathVariable Long id) {
        FormDto form = formService.getFormById(id);
        return ApiResponse.success(form);
    }

    /**
     * 获取表单设计数据
     */
    @GetMapping("/{id}/design")
    public ApiResponse<FormDesignRequest> getFormDesign(@PathVariable Long id) {
        FormDesignRequest design = formService.getFormDesign(id);
        return ApiResponse.success(design);
    }

    /**
     * 创建空白表单
     */
    @PostMapping("/create")
    public ApiResponse<FormDto> createForm(@RequestBody FormDesignRequest request) {
        FormDto form = formService.createForm(request);
        return ApiResponse.success(form);
    }

    /**
     * 自动生成表单
     */
    @PostMapping("/auto-generate")
    public ApiResponse<FormDto> autoGenerateForm(@RequestBody FormDesignRequest request) {
        FormDto form = formService.autoGenerateForm(request);
        return ApiResponse.success(form);
    }

    /**
     * 保存表单设计
     */
    @PostMapping("/{id}/save-design")
    public ApiResponse<Void> saveFormDesign(@PathVariable Long id, @RequestBody FormDesignRequest request) {
        request.setId(id);
        formService.saveFormDesign(request);
        return ApiResponse.success();
    }

    /**
     * 更新表单基本信息
     */
    @PutMapping("/{id}")
    public ApiResponse<FormDto> updateForm(@PathVariable Long id, @RequestBody FormDesignRequest request) {
        FormDto form = formService.updateForm(id, request);
        return ApiResponse.success(form);
    }

    /**
     * 删除表单
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteForm(@PathVariable Long id) {
        formService.deleteForm(id);
        return ApiResponse.success();
    }

    /**
     * 发布表单
     */
    @PostMapping("/{id}/publish")
    public ApiResponse<Void> publishForm(@PathVariable Long id) {
        formService.publishForm(id);
        return ApiResponse.success();
    }

    /**
     * 取消发布表单
     */
    @PostMapping("/{id}/unpublish")
    public ApiResponse<Void> unpublishForm(@PathVariable Long id) {
        formService.unpublishForm(id);
        return ApiResponse.success();
    }

    /**
     * 设置默认表单
     */
    @PostMapping("/{id}/set-default")
    public ApiResponse<Void> setDefaultForm(@PathVariable Long id) {
        formService.setDefaultForm(id);
        return ApiResponse.success();
    }

    /**
     * 获取表单操作日志
     */
    @GetMapping("/{id}/logs")
    public ApiResponse<List<FormLogDto>> getFormLogs(@PathVariable Long id) {
        List<FormLogDto> logs = formLogService.getFormLogs(id);
        return ApiResponse.success(logs);
    }

    /**
     * 获取表单下待质检的数据
     */
    @GetMapping("/{id}/pending-qc-data")
    public ApiResponse<List<Map<String, Object>>> getPendingQcData(@PathVariable Long id) {
        try {
            // 获取表单信息
            FormDto form = formService.getFormById(id);
            log.info("获取表单信息: formId={}, formName={}, viewId={}", id, form.getFormName(), form.getViewId());

            if (form.getViewId() == null) {
                log.warn("表单没有关联视图: formId={}", id);
                return ApiResponse.success(new ArrayList<>());
            }

            // 根据视图ID获取主实体的表名
            String tableName = getTableNameByViewId(form.getViewId());
            log.info("获取表名: viewId={}, tableName={}", form.getViewId(), tableName);

            if (tableName == null || tableName.isEmpty()) {
                log.warn("未找到表名: viewId={}", form.getViewId());
                return ApiResponse.success(new ArrayList<>());
            }

            // 查询待质检状态的数据（使用通用字段）
            String sql = String.format(
                "SELECT id, status, created_at FROM %s WHERE status = 'PENDING_QC' ORDER BY created_at DESC",
                tableName
            );
            log.info("执行查询SQL: {}", sql);

            Query query = entityManager.createNativeQuery(sql);
            @SuppressWarnings("unchecked")
            List<Object[]> rows = query.getResultList();
            log.info("查询结果数量: {}", rows.size());

            List<Map<String, Object>> result = new ArrayList<>();
            for (Object[] row : rows) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", row[0]);
                item.put("code", String.valueOf(row[0])); // 使用ID作为code
                item.put("name", tableName + "-" + row[0]); // 使用表名+ID作为name
                item.put("status", row[1] != null ? row[1].toString() : "");
                item.put("createdAt", row[2]);
                result.add(item);
            }
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("获取待质检数据失败: formId={}", id, e);
            return ApiResponse.success(new ArrayList<>());
        }
    }

    /**
     * 根据视图ID获取主实体的表名
     */
    private String getTableNameByViewId(Long viewId) {
        try {
            // 从视图实体表获取主实体的表名
            String sql = "SELECT table_name FROM std_view_entity WHERE view_id = ? AND entity_type = 'main' LIMIT 1";
            Query query = entityManager.createNativeQuery(sql);
            query.setParameter(1, viewId);
            Object result = query.getResultList().stream().findFirst().orElse(null);
            if (result != null) {
                return result.toString();
            }
        } catch (Exception e) {
            log.error("获取表名失败: viewId={}", viewId, e);
        }
        return null;
    }
}
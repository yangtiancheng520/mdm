package com.vueadmin.controller.quality;

import com.vueadmin.dto.ApiResponse;
import com.vueadmin.dto.QualityRuleDto;
import com.vueadmin.dto.ViewDefinitionDto;
import com.vueadmin.service.quality.QualityRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 质量规则Controller
 */
@RestController
@RequestMapping("/api/quality/rule")
@RequiredArgsConstructor
@Tag(name = "质量规则管理", description = "质量规则的增删改查操作")
public class QualityRuleController {

    private final QualityRuleService qualityRuleService;

    /**
     * 获取规则列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取规则列表", description = "支持按视图、实体、类型、状态筛选")
    public ApiResponse<List<QualityRuleDto>> getRuleList(
            @RequestParam(required = false) Long viewId,
            @RequestParam(required = false) Long entityId,
            @RequestParam(required = false) String ruleType,
            @RequestParam(required = false) String status) {
        List<QualityRuleDto> rules = qualityRuleService.getRuleList(viewId, entityId, ruleType, status);
        return ApiResponse.success(rules);
    }

    /**
     * 获取规则详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取规则详情")
    public ApiResponse<QualityRuleDto> getRuleById(@PathVariable Long id) {
        QualityRuleDto rule = qualityRuleService.getRuleById(id);
        return ApiResponse.success(rule);
    }

    /**
     * 创建规则
     */
    @PostMapping("/create")
    @Operation(summary = "创建规则")
    public ApiResponse<QualityRuleDto> createRule(@RequestBody QualityRuleDto dto) {
        QualityRuleDto rule = qualityRuleService.createRule(dto);
        return ApiResponse.success(rule);
    }

    /**
     * 更新规则
     */
    @PutMapping("/update/{id}")
    @Operation(summary = "更新规则")
    public ApiResponse<QualityRuleDto> updateRule(@PathVariable Long id, @RequestBody QualityRuleDto dto) {
        QualityRuleDto rule = qualityRuleService.updateRule(id, dto);
        return ApiResponse.success(rule);
    }

    /**
     * 删除规则
     */
    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除规则")
    public ApiResponse<Void> deleteRule(@PathVariable Long id) {
        qualityRuleService.deleteRule(id);
        return ApiResponse.success(null);
    }

    /**
     * 批量删除规则
     */
    @PostMapping("/batch-delete")
    @Operation(summary = "批量删除规则")
    public ApiResponse<Void> batchDeleteRules(@RequestBody List<Long> ids) {
        qualityRuleService.batchDeleteRules(ids);
        return ApiResponse.success(null);
    }

    /**
     * 启用/停用规则
     */
    @PostMapping("/toggle/{id}")
    @Operation(summary = "启用/停用规则")
    public ApiResponse<QualityRuleDto> toggleStatus(@PathVariable Long id) {
        QualityRuleDto rule = qualityRuleService.toggleStatus(id);
        return ApiResponse.success(rule);
    }

    /**
     * 获取视图的实体和字段信息（用于规则配置）
     */
    @GetMapping("/view-entities/{viewId}")
    @Operation(summary = "获取视图实体信息", description = "获取视图的实体和字段信息，用于配置规则时选择")
    public ApiResponse<ViewDefinitionDto> getViewWithEntities(@PathVariable Long viewId) {
        ViewDefinitionDto view = qualityRuleService.getViewWithEntities(viewId);
        return ApiResponse.success(view);
    }

    /**
     * 根据实体ID列表获取规则
     */
    @PostMapping("/by-entities")
    @Operation(summary = "根据实体获取规则")
    public ApiResponse<List<QualityRuleDto>> getRulesByEntityIds(@RequestBody List<Long> entityIds) {
        List<QualityRuleDto> rules = qualityRuleService.getRulesByEntityIds(entityIds);
        return ApiResponse.success(rules);
    }

    /**
     * 测试规则（只检测前100条数据）
     */
    @PostMapping("/test")
    @Operation(summary = "测试规则")
    public ApiResponse<Map<String, Object>> testRule(@RequestBody QualityRuleDto dto) {
        Map<String, Object> result = qualityRuleService.testRule(dto);
        return ApiResponse.success(result);
    }

    /**
     * 应用模板到规则
     */
    @PostMapping("/apply-template/{templateId}")
    @Operation(summary = "应用模板到规则")
    public ApiResponse<QualityRuleDto> applyTemplate(
            @PathVariable Long templateId,
            @RequestBody QualityRuleDto dto) {
        QualityRuleDto rule = qualityRuleService.applyTemplate(templateId, dto);
        return ApiResponse.success(rule);
    }
}

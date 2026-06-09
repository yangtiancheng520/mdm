package com.vueadmin.controller.quality;

import com.vueadmin.dto.ApiResponse;
import com.vueadmin.dto.QualityRuleTemplateDto;
import com.vueadmin.service.quality.QualityRuleTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 质量规则模板Controller
 */
@RestController
@RequestMapping("/api/quality/template")
@RequiredArgsConstructor
@Tag(name = "质量规则模板管理", description = "质量规则模板的增删改查操作")
public class QualityRuleTemplateController {

    private final QualityRuleTemplateService templateService;

    /**
     * 获取模板列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取模板列表")
    public ApiResponse<List<QualityRuleTemplateDto>> getTemplateList(
            @RequestParam(required = false) String templateType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String isSystem) {
        List<QualityRuleTemplateDto> templates = templateService.getTemplateList(templateType, status, isSystem);
        return ApiResponse.success(templates);
    }

    /**
     * 获取模板详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取模板详情")
    public ApiResponse<QualityRuleTemplateDto> getTemplateById(@PathVariable Long id) {
        QualityRuleTemplateDto template = templateService.getTemplateById(id);
        return ApiResponse.success(template);
    }

    /**
     * 创建模板
     */
    @PostMapping("/create")
    @Operation(summary = "创建模板")
    public ApiResponse<QualityRuleTemplateDto> createTemplate(@RequestBody QualityRuleTemplateDto dto) {
        QualityRuleTemplateDto template = templateService.createTemplate(dto, "system");
        return ApiResponse.success(template);
    }

    /**
     * 更新模板
     */
    @PutMapping("/update/{id}")
    @Operation(summary = "更新模板")
    public ApiResponse<QualityRuleTemplateDto> updateTemplate(
            @PathVariable Long id,
            @RequestBody QualityRuleTemplateDto dto) {
        QualityRuleTemplateDto template = templateService.updateTemplate(id, dto, "system");
        return ApiResponse.success(template);
    }

    /**
     * 删除模板
     */
    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除模板")
    public ApiResponse<Void> deleteTemplate(@PathVariable Long id) {
        templateService.deleteTemplate(id);
        return ApiResponse.success(null);
    }

    /**
     * 发布模板（草稿 -> 启用）
     */
    @PostMapping("/publish/{id}")
    @Operation(summary = "发布模板")
    public ApiResponse<QualityRuleTemplateDto> publishTemplate(@PathVariable Long id) {
        QualityRuleTemplateDto template = templateService.publishTemplate(id);
        return ApiResponse.success(template);
    }

    /**
     * 重置模板（启用 -> 草稿）
     */
    @PostMapping("/reset/{id}")
    @Operation(summary = "重置模板")
    public ApiResponse<QualityRuleTemplateDto> resetTemplate(@PathVariable Long id) {
        QualityRuleTemplateDto template = templateService.resetTemplate(id);
        return ApiResponse.success(template);
    }

    /**
     * 复制模板
     */
    @PostMapping("/copy/{id}")
    @Operation(summary = "复制模板")
    public ApiResponse<QualityRuleTemplateDto> copyTemplate(
            @PathVariable Long id,
            @RequestParam(required = false) String newName) {
        QualityRuleTemplateDto template = templateService.copyTemplate(id, newName, "system");
        return ApiResponse.success(template);
    }
}

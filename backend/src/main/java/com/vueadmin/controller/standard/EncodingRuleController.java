package com.vueadmin.controller.standard;

import com.vueadmin.dto.ApiResponse;
import com.vueadmin.dto.EncodingRuleDto;
import com.vueadmin.dto.PageResult;
import com.vueadmin.service.standard.EncodingRuleService;
import com.vueadmin.service.standard.encoding.EncodingGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 编码规则控制器
 */
@RestController
@RequestMapping("/api/encoding-rule")
@RequiredArgsConstructor
public class EncodingRuleController {

    private final EncodingRuleService ruleService;
    private final EncodingGeneratorService generatorService;

    /**
     * 获取规则列表
     */
    @GetMapping("/list")
    public ApiResponse<PageResult<EncodingRuleDto>> list(
            @RequestParam(required = false) String ruleCode,
            @RequestParam(required = false) String ruleName,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(ruleService.search(ruleCode, ruleName, status, page, size));
    }

    /**
     * 获取所有启用的规则
     */
    @GetMapping("/active")
    public ApiResponse<List<EncodingRuleDto>> getActiveRules() {
        return ApiResponse.success(ruleService.getActiveRules());
    }

    /**
     * 根据ID获取规则
     */
    @GetMapping("/{id}")
    public ApiResponse<EncodingRuleDto> getById(@PathVariable Long id) {
        return ApiResponse.success(ruleService.getById(id));
    }

    /**
     * 根据编码获取规则
     */
    @GetMapping("/code/{ruleCode}")
    public ApiResponse<EncodingRuleDto> getByCode(@PathVariable String ruleCode) {
        return ApiResponse.success(ruleService.getByCode(ruleCode));
    }

    /**
     * 创建规则
     */
    @PostMapping("/create")
    public ApiResponse<EncodingRuleDto> create(@RequestBody EncodingRuleDto dto) {
        return ApiResponse.success(ruleService.create(dto));
    }

    /**
     * 更新规则
     */
    @PutMapping("/update/{id}")
    public ApiResponse<EncodingRuleDto> update(@PathVariable Long id, @RequestBody EncodingRuleDto dto) {
        return ApiResponse.success(ruleService.update(id, dto));
    }

    /**
     * 删除规则
     */
    @DeleteMapping("/delete/{id}")
    public ApiResponse<?> delete(@PathVariable Long id) {
        ruleService.delete(id);
        return ApiResponse.success();
    }

    /**
     * 批量删除
     */
    @PostMapping("/batch-delete")
    public ApiResponse<?> batchDelete(@RequestBody List<Long> ids) {
        ruleService.batchDelete(ids);
        return ApiResponse.success();
    }

    /**
     * 生成编码
     */
    @PostMapping("/generate/{ruleCode}")
    public ApiResponse<String> generate(
            @PathVariable String ruleCode,
            @RequestBody(required = false) Map<String, Object> businessData) {
        return ApiResponse.success(generatorService.generate(ruleCode, businessData));
    }

    /**
     * 预览编码
     */
    @PostMapping("/preview/{ruleCode}")
    public ApiResponse<String> preview(
            @PathVariable String ruleCode,
            @RequestBody(required = false) Map<String, Object> businessData) {
        return ApiResponse.success(generatorService.preview(ruleCode, businessData));
    }
}

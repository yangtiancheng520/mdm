package com.vueadmin.controller;

import com.vueadmin.dto.*;
import com.vueadmin.service.system.RuleScriptService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 规则脚本控制器
 */
@RestController
@RequestMapping("/api/rule-script")
@RequiredArgsConstructor
public class RuleScriptController {

    private final RuleScriptService ruleScriptService;

    @GetMapping("/list")
    public ApiResponse<PageResult<RuleScriptDto>> list(
            @RequestParam(required = false) String scriptCode,
            @RequestParam(required = false) String scriptName,
            @RequestParam(required = false) String scriptType,
            @RequestParam(required = false) String status) {
        return ApiResponse.success(ruleScriptService.search(scriptCode, scriptName, scriptType, status));
    }

    @GetMapping("/active")
    public ApiResponse<List<RuleScriptDto>> getActiveScripts() {
        return ApiResponse.success(ruleScriptService.getActiveScripts());
    }

    @GetMapping("/{id}")
    public ApiResponse<RuleScriptDto> getById(@PathVariable Long id) {
        return ApiResponse.success(ruleScriptService.getById(id));
    }

    @GetMapping("/code/{scriptCode}")
    public ApiResponse<RuleScriptDto> getByCode(@PathVariable String scriptCode) {
        return ApiResponse.success(ruleScriptService.getByCode(scriptCode));
    }

    @PostMapping("/create")
    public ApiResponse<RuleScriptDto> create(@RequestBody RuleScriptDto dto) {
        com.vueadmin.entity.system.RuleScript script = ruleScriptService.create(dto);
        RuleScriptDto result = new RuleScriptDto();
        result.setId(script.getId());
        result.setScriptCode(script.getScriptCode());
        return ApiResponse.success(result);
    }

    @PutMapping("/update/{id}")
    public ApiResponse<?> update(@PathVariable Long id, @RequestBody RuleScriptDto dto) {
        ruleScriptService.update(id, dto);
        return ApiResponse.success();
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<?> delete(@PathVariable Long id) {
        ruleScriptService.delete(id);
        return ApiResponse.success();
    }

    @PostMapping("/test/{id}")
    public ApiResponse<Map<String, Object>> testScript(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> params) {
        String testInput = params != null ? params.get("testInput") : null;
        return ApiResponse.success(ruleScriptService.testScript(id, testInput));
    }

    @PostMapping("/execute/{scriptCode}")
    public ApiResponse<Object> executeScript(
            @PathVariable String scriptCode,
            @RequestBody(required = false) Map<String, Object> params) {
        return ApiResponse.success(ruleScriptService.executeByCode(scriptCode, params));
    }
}

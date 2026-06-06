package com.vueadmin.controller;

import com.vueadmin.dto.*;
import com.vueadmin.service.system.SystemConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 系统配置控制器
 */
@RestController
@RequestMapping("/api/system-config")
@RequiredArgsConstructor
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    @GetMapping("/list")
    public ApiResponse<PageResult<SystemConfigDto>> list(
            @RequestParam(required = false) String configKey,
            @RequestParam(required = false) String configGroup) {
        return ApiResponse.success(systemConfigService.search(configKey, configGroup));
    }

    @GetMapping("/groups")
    public ApiResponse<List<String>> getAllGroups() {
        return ApiResponse.success(systemConfigService.getAllGroups());
    }

    @GetMapping("/group/{configGroup}")
    public ApiResponse<List<SystemConfigDto>> getByGroup(@PathVariable String configGroup) {
        return ApiResponse.success(systemConfigService.getByGroup(configGroup));
    }

    @GetMapping("/{id}")
    public ApiResponse<SystemConfigDto> getById(@PathVariable Long id) {
        return ApiResponse.success(systemConfigService.getById(id));
    }

    @GetMapping("/key/{configKey}")
    public ApiResponse<String> getByKey(@PathVariable String configKey) {
        return ApiResponse.success(systemConfigService.getConfigValue(configKey));
    }

    @GetMapping("/all")
    public ApiResponse<Map<String, String>> getAllConfigs() {
        return ApiResponse.success(systemConfigService.getAllConfigsMap());
    }

    @PostMapping("/create")
    public ApiResponse<SystemConfigDto> create(@RequestBody SystemConfigDto dto) {
        com.vueadmin.entity.system.SystemConfig config = systemConfigService.create(dto);
        SystemConfigDto result = new SystemConfigDto();
        result.setId(config.getId());
        result.setConfigKey(config.getConfigKey());
        return ApiResponse.success(result);
    }

    @PutMapping("/update/{id}")
    public ApiResponse<?> update(@PathVariable Long id, @RequestBody SystemConfigDto dto) {
        systemConfigService.update(id, dto);
        return ApiResponse.success();
    }

    @PutMapping("/value")
    public ApiResponse<?> updateValue(@RequestBody Map<String, String> params) {
        String configKey = params.get("configKey");
        String configValue = params.get("configValue");
        systemConfigService.updateValue(configKey, configValue);
        return ApiResponse.success();
    }

    @PostMapping("/batch-update")
    public ApiResponse<?> batchUpdate(@RequestBody Map<String, String> configs) {
        systemConfigService.batchUpdate(configs);
        return ApiResponse.success();
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<?> delete(@PathVariable Long id) {
        systemConfigService.delete(id);
        return ApiResponse.success();
    }

    @PostMapping("/refresh-cache")
    public ApiResponse<?> refreshCache() {
        systemConfigService.refreshCache();
        return ApiResponse.success("缓存已刷新");
    }
}

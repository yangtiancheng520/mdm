package com.vueadmin.controller;

import com.vueadmin.dto.*;
import com.vueadmin.service.system.IntegrationConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 外部集成配置控制器
 */
@RestController
@RequestMapping("/api/integration")
@RequiredArgsConstructor
public class IntegrationConfigController {

    private final IntegrationConfigService integrationConfigService;

    @GetMapping("/list")
    public ApiResponse<PageResult<IntegrationConfigDto>> list(
            @RequestParam(required = false) String integrationCode,
            @RequestParam(required = false) String integrationName,
            @RequestParam(required = false) String integrationType,
            @RequestParam(required = false) String status) {
        return ApiResponse.success(integrationConfigService.search(
                integrationCode, integrationName, integrationType, status));
    }

    @GetMapping("/active")
    public ApiResponse<List<IntegrationConfigDto>> getActiveConfigs() {
        return ApiResponse.success(integrationConfigService.getActiveConfigs());
    }

    @GetMapping("/type/{integrationType}")
    public ApiResponse<List<IntegrationConfigDto>> getByType(@PathVariable String integrationType) {
        return ApiResponse.success(integrationConfigService.getByType(integrationType));
    }

    @GetMapping("/{id}")
    public ApiResponse<IntegrationConfigDto> getById(@PathVariable Long id) {
        return ApiResponse.success(integrationConfigService.getById(id));
    }

    @GetMapping("/code/{integrationCode}")
    public ApiResponse<IntegrationConfigDto> getByCode(@PathVariable String integrationCode) {
        return ApiResponse.success(integrationConfigService.getByCode(integrationCode));
    }

    @PostMapping("/create")
    public ApiResponse<IntegrationConfigDto> create(@RequestBody IntegrationConfigDto dto) {
        com.vueadmin.entity.system.IntegrationConfig config = integrationConfigService.create(dto);
        IntegrationConfigDto result = new IntegrationConfigDto();
        result.setId(config.getId());
        result.setIntegrationCode(config.getIntegrationCode());
        return ApiResponse.success(result);
    }

    @PutMapping("/update/{id}")
    public ApiResponse<?> update(@PathVariable Long id, @RequestBody IntegrationConfigDto dto) {
        integrationConfigService.update(id, dto);
        return ApiResponse.success();
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<?> delete(@PathVariable Long id) {
        integrationConfigService.delete(id);
        return ApiResponse.success();
    }

    @PostMapping("/test/{id}")
    public ApiResponse<Map<String, Object>> testConnection(@PathVariable Long id) {
        return ApiResponse.success(integrationConfigService.testConnection(id));
    }

    @PostMapping("/call/{integrationCode}")
    public ApiResponse<Map<String, Object>> callApi(
            @PathVariable String integrationCode,
            @RequestBody(required = false) Map<String, Object> params) {
        String apiPath = params != null ? (String) params.remove("apiPath") : null;
        return ApiResponse.success(integrationConfigService.callApi(integrationCode, apiPath, params));
    }
}

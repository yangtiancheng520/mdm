package com.vueadmin.service.system;

import com.alibaba.fastjson2.JSON;
import com.vueadmin.dto.IntegrationConfigDto;
import com.vueadmin.dto.PageResult;
import com.vueadmin.entity.system.IntegrationConfig;
import com.vueadmin.repository.IntegrationConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 外部集成配置服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IntegrationConfigService {

    private final IntegrationConfigRepository integrationConfigRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // 集成类型映射
    private static final Map<String, String> INTEGRATION_TYPE_MAP = new HashMap<>() {{
        put("tianyancha", "天眼查");
        put("lbpm", "流程引擎");
        put("data_platform", "数据中台");
        put("erp", "ERP系统");
        put("crm", "CRM系统");
        put("custom", "自定义集成");
    }};

    // 认证类型映射
    private static final Map<String, String> AUTH_TYPE_MAP = new HashMap<>() {{
        put("none", "无认证");
        put("basic", "Basic认证");
        put("oauth2", "OAuth2");
        put("apikey", "API Key");
    }};

    /**
     * 搜索集成配置
     */
    public PageResult<IntegrationConfigDto> search(String integrationCode, String integrationName,
                                                    String integrationType, String status) {
        List<IntegrationConfig> configs = integrationConfigRepository.searchConfigs(
                integrationCode, integrationName, integrationType, status);
        List<IntegrationConfigDto> list = configs.stream().map(this::toDto).collect(Collectors.toList());
        return new PageResult<>(list, (long) list.size());
    }

    /**
     * 获取所有启用的集成配置
     */
    public List<IntegrationConfigDto> getActiveConfigs() {
        return integrationConfigRepository.findByStatus("active").stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 根据类型获取集成配置
     */
    public List<IntegrationConfigDto> getByType(String integrationType) {
        return integrationConfigRepository.findByIntegrationType(integrationType).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 获取集成配置详情
     */
    public IntegrationConfigDto getById(Long id) {
        return integrationConfigRepository.findById(id)
                .map(this::toDto)
                .orElse(null);
    }

    /**
     * 根据编码获取配置
     */
    public IntegrationConfigDto getByCode(String integrationCode) {
        return integrationConfigRepository.findByIntegrationCode(integrationCode)
                .map(this::toDto)
                .orElse(null);
    }

    /**
     * 创建集成配置
     */
    @Transactional
    public IntegrationConfig create(IntegrationConfigDto dto) {
        if (integrationConfigRepository.existsByIntegrationCode(dto.getIntegrationCode())) {
            throw new RuntimeException("集成编码已存在: " + dto.getIntegrationCode());
        }

        IntegrationConfig config = new IntegrationConfig();
        config.setIntegrationCode(dto.getIntegrationCode());
        config.setIntegrationName(dto.getIntegrationName());
        config.setIntegrationType(dto.getIntegrationType());
        config.setApiEndpoint(dto.getApiEndpoint());
        config.setAuthType(dto.getAuthType());
        config.setAuthConfig(dto.getAuthConfig());
        config.setRequestConfig(dto.getRequestConfig());
        config.setMappingConfig(dto.getMappingConfig());
        config.setStatus(dto.getStatus() != null ? dto.getStatus() : "active");
        config.setCreatedBy(dto.getCreatedBy());
        config.setDescription(dto.getDescription());

        return integrationConfigRepository.save(config);
    }

    /**
     * 更新集成配置
     */
    @Transactional
    public void update(Long id, IntegrationConfigDto dto) {
        IntegrationConfig config = integrationConfigRepository.findById(id).orElse(null);
        if (config == null) return;

        config.setIntegrationName(dto.getIntegrationName());
        config.setIntegrationType(dto.getIntegrationType());
        config.setApiEndpoint(dto.getApiEndpoint());
        config.setAuthType(dto.getAuthType());
        config.setAuthConfig(dto.getAuthConfig());
        config.setRequestConfig(dto.getRequestConfig());
        config.setMappingConfig(dto.getMappingConfig());
        config.setStatus(dto.getStatus());
        config.setUpdatedBy(dto.getUpdatedBy());
        config.setDescription(dto.getDescription());

        integrationConfigRepository.save(config);
    }

    /**
     * 删除集成配置
     */
    @Transactional
    public void delete(Long id) {
        integrationConfigRepository.deleteById(id);
    }

    /**
     * 测试连接
     */
    public Map<String, Object> testConnection(Long id) {
        IntegrationConfig config = integrationConfigRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("集成配置不存在"));

        Map<String, Object> result = new HashMap<>();
        result.put("integrationCode", config.getIntegrationCode());
        result.put("integrationName", config.getIntegrationName());

        try {
            // 构建请求
            HttpHeaders headers = buildHeaders(config);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // 发送测试请求
            String testUrl = config.getApiEndpoint();
            if (!testUrl.endsWith("/health") && !testUrl.endsWith("/ping")) {
                testUrl = testUrl.endsWith("/") ? testUrl + "health" : testUrl + "/health";
            }

            long startTime = System.currentTimeMillis();
            ResponseEntity<String> response = restTemplate.exchange(
                    testUrl, HttpMethod.GET, entity, String.class);
            long endTime = System.currentTimeMillis();

            result.put("success", response.getStatusCode().is2xxSuccessful());
            result.put("statusCode", response.getStatusCode().value());
            result.put("responseTime", (endTime - startTime) + "ms");
            result.put("response", response.getBody());
            result.put("testTime", LocalDateTime.now().format(formatter));

        } catch (Exception e) {
            log.error("测试连接失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("testTime", LocalDateTime.now().format(formatter));
        }

        return result;
    }

    /**
     * 调用外部API
     */
    public Map<String, Object> callApi(String integrationCode, String apiPath, Map<String, Object> params) {
        IntegrationConfig config = integrationConfigRepository.findByIntegrationCode(integrationCode)
                .orElseThrow(() -> new RuntimeException("集成配置不存在: " + integrationCode));

        if (!"active".equals(config.getStatus())) {
            throw new RuntimeException("集成配置未启用: " + integrationCode);
        }

        try {
            HttpHeaders headers = buildHeaders(config);
            String url = config.getApiEndpoint();
            if (apiPath != null && !apiPath.isEmpty()) {
                url = url.endsWith("/") ? url + apiPath.substring(1) : url + apiPath;
            }

            // 解析请求配置
            Map<String, Object> requestConfig = new HashMap<>();
            if (config.getRequestConfig() != null) {
                requestConfig = JSON.parseObject(config.getRequestConfig(), Map.class);
            }
            int timeout = (int) requestConfig.getOrDefault("timeout", 30000);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(params, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, Map.class);

            Map<String, Object> result = new HashMap<>();
            result.put("success", response.getStatusCode().is2xxSuccessful());
            result.put("data", response.getBody());
            return result;

        } catch (Exception e) {
            log.error("调用外部API失败", e);
            throw new RuntimeException("调用失败: " + e.getMessage());
        }
    }

    /**
     * 构建请求头
     */
    private HttpHeaders buildHeaders(IntegrationConfig config) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (config.getAuthConfig() != null && !"none".equals(config.getAuthType())) {
            Map<String, Object> authConfig = JSON.parseObject(config.getAuthConfig(), Map.class);

            switch (config.getAuthType()) {
                case "basic":
                    String username = (String) authConfig.get("username");
                    String password = (String) authConfig.get("password");
                    headers.setBasicAuth(username, password);
                    break;
                case "apikey":
                    String apiKey = (String) authConfig.get("apiKey");
                    String headerName = (String) authConfig.getOrDefault("headerName", "X-API-Key");
                    headers.set(headerName, apiKey);
                    break;
                case "oauth2":
                    String token = (String) authConfig.get("accessToken");
                    headers.setBearerAuth(token);
                    break;
            }
        }

        return headers;
    }

    /**
     * 转换为DTO
     */
    private IntegrationConfigDto toDto(IntegrationConfig config) {
        IntegrationConfigDto dto = new IntegrationConfigDto();
        dto.setId(config.getId());
        dto.setIntegrationCode(config.getIntegrationCode());
        dto.setIntegrationName(config.getIntegrationName());
        dto.setIntegrationType(config.getIntegrationType());
        dto.setApiEndpoint(config.getApiEndpoint());
        dto.setAuthType(config.getAuthType());
        dto.setAuthConfig(config.getAuthConfig());
        dto.setRequestConfig(config.getRequestConfig());
        dto.setMappingConfig(config.getMappingConfig());
        dto.setStatus(config.getStatus());
        dto.setCreatedBy(config.getCreatedBy());
        dto.setUpdatedBy(config.getUpdatedBy());
        dto.setDescription(config.getDescription());

        // 设置显示名称
        dto.setIntegrationTypeName(INTEGRATION_TYPE_MAP.getOrDefault(
                config.getIntegrationType(), config.getIntegrationType()));
        dto.setAuthTypeName(AUTH_TYPE_MAP.getOrDefault(
                config.getAuthType(), config.getAuthType()));

        // 格式化时间
        if (config.getCreatedAt() != null) {
            dto.setCreatedAt(config.getCreatedAt().format(formatter));
        }
        if (config.getUpdatedAt() != null) {
            dto.setUpdatedAt(config.getUpdatedAt().format(formatter));
        }

        return dto;
    }
}

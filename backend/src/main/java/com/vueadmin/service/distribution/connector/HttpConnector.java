package com.vueadmin.service.distribution.connector;

import com.vueadmin.dto.distribution.DistributionResultDTO;
import com.vueadmin.entity.distribution.DisSystemConfig;
import com.vueadmin.exception.BusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * HTTP连接器 - 基于REST API实现
 */
public class HttpConnector implements SystemConnector {

    private static final Logger logger = LoggerFactory.getLogger(HttpConnector.class);

    private final DisSystemConfig config;
    private final Map<String, Object> connectionConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public HttpConnector(DisSystemConfig config, Map<String, Object> connectionConfig) {
        this.config = config;
        this.connectionConfig = connectionConfig;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public boolean testConnection() {
        try {
            String url = getBaseUrl();
            String method = getString("method", "GET");

            HttpHeaders headers = buildHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.valueOf(method),
                    entity,
                    String.class
            );

            boolean success = response.getStatusCode().is2xxSuccessful();
            logger.info("HTTP连接测试{}: {}", success ? "成功" : "失败", url);
            return success;

        } catch (Exception e) {
            logger.error("HTTP连接测试失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public DistributionResultDTO create(Map<String, Object> data) {
        return executeRequest("/create", HttpMethod.POST, data);
    }

    @Override
    public DistributionResultDTO update(String targetKey, Map<String, Object> data) {
        data.put("id", targetKey);
        return executeRequest("/update/" + targetKey, HttpMethod.PUT, data);
    }

    @Override
    public DistributionResultDTO delete(String targetKey) {
        return executeRequest("/delete/" + targetKey, HttpMethod.DELETE, null);
    }

    /**
     * 执行HTTP请求
     */
    private DistributionResultDTO executeRequest(String path, HttpMethod method, Map<String, Object> data) {
        try {
            String url = getBaseUrl() + path;
            HttpHeaders headers = buildHeaders();

            String body = null;
            if (data != null) {
                body = objectMapper.writeValueAsString(data);
            }

            HttpEntity<String> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    method,
                    entity,
                    String.class
            );

            return parseResponse(response);

        } catch (Exception e) {
            logger.error("HTTP请求失败: {}", e.getMessage());
            return DistributionResultDTO.fail("HTTP_ERROR", e.getMessage());
        }
    }

    /**
     * 构建请求头
     */
    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String authType = getString("authType", "None");

        switch (authType) {
            case "Bearer":
                String token = getString("token");
                if (!token.isEmpty()) {
                    headers.setBearerAuth(token);
                }
                break;
            case "Basic":
                String username = getString("username");
                String password = getString("password");
                if (!username.isEmpty()) {
                    headers.setBasicAuth(username, password);
                }
                break;
            case "ApiKey":
                String apiKey = getString("apiKey");
                String headerName = getString("apiKeyHeader", "X-API-Key");
                if (!apiKey.isEmpty()) {
                    headers.set(headerName, apiKey);
                }
                break;
        }

        // 添加自定义请求头
        @SuppressWarnings("unchecked")
        Map<String, String> customHeaders = (Map<String, String>) connectionConfig.get("headers");
        if (customHeaders != null) {
            customHeaders.forEach(headers::set);
        }

        return headers;
    }

    /**
     * 解析响应
     */
    @SuppressWarnings("unchecked")
    private DistributionResultDTO parseResponse(ResponseEntity<String> response) {
        DistributionResultDTO result = new DistributionResultDTO();

        if (response.getStatusCode().is2xxSuccessful()) {
            result.setSuccess(true);
            result.setMessage("执行成功");

            try {
                Map<String, Object> body = objectMapper.readValue(response.getBody(), Map.class);

                // 尝试获取返回的ID
                Object id = body.get("id");
                if (id == null) {
                    id = body.get("data");
                }
                if (id != null) {
                    result.setTargetKey(id.toString());
                }

                // 尝试获取消息
                Object message = body.get("message");
                if (message != null) {
                    result.setMessage(message.toString());
                }

                result.setResponseData(body);

            } catch (Exception e) {
                logger.warn("解析响应体失败: {}", e.getMessage());
            }

        } else {
            result.setSuccess(false);
            result.setErrorMsg("HTTP状态码: " + response.getStatusCode());
        }

        return result;
    }

    @Override
    public DisSystemConfig getConfig() {
        return config;
    }

    @Override
    public String getSystemType() {
        return "HTTP";
    }

    private String getBaseUrl() {
        return getString("url");
    }

    private String getString(String key) {
        Object value = connectionConfig.get(key);
        return value != null ? value.toString() : "";
    }

    private String getString(String key, String defaultValue) {
        Object value = connectionConfig.get(key);
        return value != null ? value.toString() : defaultValue;
    }
}

package com.vueadmin.service.distribution.connector;

import com.vueadmin.entity.distribution.DisSystemConfig;
import com.vueadmin.repository.DisSystemConfigRepository;
import com.vueadmin.exception.BusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 连接器工厂
 */
@Component
public class ConnectorFactory {

    @Autowired
    private DisSystemConfigRepository systemConfigRepository;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 连接器缓存
     */
    private final Map<Long, SystemConnector> connectorCache = new ConcurrentHashMap<>();

    /**
     * 根据配置ID获取连接器
     */
    public SystemConnector getConnector(Long configId) {
        // 先从缓存获取
        if (connectorCache.containsKey(configId)) {
            return connectorCache.get(configId);
        }

        // 从数据库加载配置
        DisSystemConfig config = systemConfigRepository.findById(configId)
                .orElseThrow(() -> new BusinessException("系统配置不存在: " + configId));

        // 创建连接器
        SystemConnector connector = createConnector(config);

        // 放入缓存
        connectorCache.put(configId, connector);

        return connector;
    }

    /**
     * 根据配置获取连接器
     */
    public SystemConnector getConnector(DisSystemConfig config) {
        if (connectorCache.containsKey(config.getId())) {
            return connectorCache.get(config.getId());
        }

        SystemConnector connector = createConnector(config);
        connectorCache.put(config.getId(), connector);

        return connector;
    }

    /**
     * 创建连接器
     */
    private SystemConnector createConnector(DisSystemConfig config) {
        String systemType = config.getSystemType();

        // 解析连接配置
        Map<String, Object> connectionConfig = parseConnectionConfig(config.getConnectionConfig());

        switch (systemType) {
            case "SAP":
                return new SapConnector(config, connectionConfig);
            case "HTTP":
                return new HttpConnector(config, connectionConfig);
            case "DATABASE":
                return new DatabaseConnector(config, connectionConfig);
            default:
                throw new BusinessException("不支持的系统类型: " + systemType);
        }
    }

    /**
     * 解析连接配置JSON
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseConnectionConfig(String configJson) {
        try {
            return objectMapper.readValue(configJson, Map.class);
        } catch (Exception e) {
            throw new BusinessException("解析连接配置失败: " + e.getMessage());
        }
    }

    /**
     * 清除缓存
     */
    public void clearCache(Long configId) {
        connectorCache.remove(configId);
    }

    /**
     * 清除所有缓存
     */
    public void clearAllCache() {
        connectorCache.clear();
    }
}

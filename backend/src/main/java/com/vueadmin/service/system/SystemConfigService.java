package com.vueadmin.service.system;

import com.vueadmin.dto.PageResult;
import com.vueadmin.dto.SystemConfigDto;
import com.vueadmin.entity.system.SystemConfig;
import com.vueadmin.repository.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 系统配置服务
 */
@Service
@RequiredArgsConstructor
public class SystemConfigService {

    private final SystemConfigRepository configRepository;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // 配置缓存
    private final Map<String, String> configCache = new HashMap<>();

    /**
     * 搜索系统配置
     */
    public PageResult<SystemConfigDto> search(String configKey, String configGroup) {
        List<SystemConfig> configs = configRepository.searchConfigs(configKey, configGroup);
        List<SystemConfigDto> list = configs.stream().map(this::toDto).collect(Collectors.toList());
        return new PageResult<>(list, (long) list.size());
    }

    /**
     * 获取所有配置分组
     */
    public List<String> getAllGroups() {
        return configRepository.findAll().stream()
                .map(SystemConfig::getConfigGroup)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 获取指定分组的配置
     */
    public List<SystemConfigDto> getByGroup(String configGroup) {
        return configRepository.findByConfigGroup(configGroup).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 获取配置详情
     */
    public SystemConfigDto getById(Long id) {
        return configRepository.findById(id)
                .map(this::toDto)
                .orElse(null);
    }

    /**
     * 根据Key获取配置值
     */
    public String getConfigValue(String configKey) {
        // 先从缓存获取
        if (configCache.containsKey(configKey)) {
            return configCache.get(configKey);
        }

        // 从数据库获取
        Optional<SystemConfig> config = configRepository.findByConfigKey(configKey);
        if (config.isPresent()) {
            String value = config.get().getConfigValue();
            configCache.put(configKey, value);
            return value;
        }

        return null;
    }

    /**
     * 根据Key获取配置值，带默认值
     */
    public String getConfigValue(String configKey, String defaultValue) {
        String value = getConfigValue(configKey);
        return value != null ? value : defaultValue;
    }

    /**
     * 根据Key获取Boolean配置
     */
    public Boolean getBooleanConfig(String configKey, Boolean defaultValue) {
        String value = getConfigValue(configKey);
        if (value == null) return defaultValue;
        return "true".equalsIgnoreCase(value) || "1".equals(value);
    }

    /**
     * 根据Key获取Integer配置
     */
    public Integer getIntegerConfig(String configKey, Integer defaultValue) {
        String value = getConfigValue(configKey);
        if (value == null) return defaultValue;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 获取所有配置Map
     */
    public Map<String, String> getAllConfigsMap() {
        Map<String, String> map = new HashMap<>();
        configRepository.findAll().forEach(config ->
            map.put(config.getConfigKey(), config.getConfigValue()));
        return map;
    }

    /**
     * 创建系统配置
     */
    @Transactional
    public SystemConfig create(SystemConfigDto dto) {
        if (configRepository.existsByConfigKey(dto.getConfigKey())) {
            throw new RuntimeException("配置键已存在: " + dto.getConfigKey());
        }

        SystemConfig config = new SystemConfig();
        config.setConfigKey(dto.getConfigKey());
        config.setConfigValue(dto.getConfigValue());
        config.setConfigType(dto.getConfigType());
        config.setConfigGroup(dto.getConfigGroup());
        config.setDescription(dto.getDescription());
        config.setIsEncrypted(dto.getIsEncrypted());
        config.setCreatedBy(dto.getCreatedBy());

        SystemConfig saved = configRepository.save(config);

        // 更新缓存
        configCache.put(saved.getConfigKey(), saved.getConfigValue());

        return saved;
    }

    /**
     * 更新系统配置
     */
    @Transactional
    public void update(Long id, SystemConfigDto dto) {
        SystemConfig config = configRepository.findById(id).orElse(null);
        if (config == null) return;

        config.setConfigValue(dto.getConfigValue());
        config.setConfigType(dto.getConfigType());
        config.setConfigGroup(dto.getConfigGroup());
        config.setDescription(dto.getDescription());
        config.setIsEncrypted(dto.getIsEncrypted());
        config.setUpdatedBy(dto.getUpdatedBy());

        configRepository.save(config);

        // 更新缓存
        configCache.put(config.getConfigKey(), config.getConfigValue());
    }

    /**
     * 更新配置值
     */
    @Transactional
    public void updateValue(String configKey, String configValue) {
        configRepository.findByConfigKey(configKey).ifPresent(config -> {
            config.setConfigValue(configValue);
            configRepository.save(config);
            configCache.put(configKey, configValue);
        });
    }

    /**
     * 批量更新配置
     */
    @Transactional
    public void batchUpdate(Map<String, String> configs) {
        configs.forEach((key, value) -> {
            configRepository.findByConfigKey(key).ifPresent(config -> {
                config.setConfigValue(value);
                configRepository.save(config);
                configCache.put(key, value);
            });
        });
    }

    /**
     * 删除系统配置
     */
    @Transactional
    public void delete(Long id) {
        configRepository.findById(id).ifPresent(config -> {
            configCache.remove(config.getConfigKey());
            configRepository.deleteById(id);
        });
    }

    /**
     * 刷新配置缓存
     */
    public void refreshCache() {
        configCache.clear();
        configRepository.findAll().forEach(config ->
            configCache.put(config.getConfigKey(), config.getConfigValue()));
    }

    /**
     * 转换为DTO
     */
    private SystemConfigDto toDto(SystemConfig config) {
        SystemConfigDto dto = new SystemConfigDto();
        dto.setId(config.getId());
        dto.setConfigKey(config.getConfigKey());
        dto.setConfigValue(config.getConfigValue());
        dto.setConfigType(config.getConfigType());
        dto.setConfigGroup(config.getConfigGroup());
        dto.setDescription(config.getDescription());
        dto.setIsEncrypted(config.getIsEncrypted());
        dto.setCreatedBy(config.getCreatedBy());
        dto.setUpdatedBy(config.getUpdatedBy());

        if (config.getCreatedAt() != null) {
            dto.setCreatedAt(config.getCreatedAt().format(formatter));
        }
        if (config.getUpdatedAt() != null) {
            dto.setUpdatedAt(config.getUpdatedAt().format(formatter));
        }

        return dto;
    }
}

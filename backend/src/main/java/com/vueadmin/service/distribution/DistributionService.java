package com.vueadmin.service.distribution;

import com.vueadmin.dto.distribution.DistributionResultDTO;
import com.vueadmin.entity.distribution.FieldMapping;
import com.vueadmin.entity.distribution.LogDistribution;
import com.vueadmin.entity.distribution.DisSystemConfig;
import com.vueadmin.exception.BusinessException;
import com.vueadmin.repository.DisSystemConfigRepository;
import com.vueadmin.repository.FieldMappingRepository;
import com.vueadmin.repository.LogDistributionRepository;
import com.vueadmin.service.distribution.connector.ConnectorFactory;
import com.vueadmin.service.distribution.connector.SystemConnector;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 分发服务
 */
@Service
public class DistributionService {

    private static final Logger logger = LoggerFactory.getLogger(DistributionService.class);

    @Autowired
    private DisSystemConfigRepository systemConfigRepository;

    @Autowired
    private FieldMappingRepository fieldMappingRepository;

    @Autowired
    private LogDistributionRepository logDistributionRepository;

    @Autowired
    private ConnectorFactory connectorFactory;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LineageService lineageService;

    /**
     * 分发单个数据
     */
    @Transactional
    public DistributionResultDTO distribute(String dataType, Long dataId, Map<String, Object> sourceData, Long systemConfigId) {
        // 获取系统配置
        DisSystemConfig config = systemConfigRepository.findById(systemConfigId)
                .orElseThrow(() -> new BusinessException("系统配置不存在"));

        if (!"active".equals(config.getStatus())) {
            throw new BusinessException("系统配置未启用");
        }

        // 获取字段映射
        List<FieldMapping> mappings = fieldMappingRepository.findActiveByDataTypeAndSystemConfig(dataType, systemConfigId);
        if (mappings.isEmpty()) {
            mappings = fieldMappingRepository.findActiveByDataType(dataType);
        }

        // 字段映射转换
        Map<String, Object> mappedData = mapFields(sourceData, mappings);

        // 添加数据类型标识，供连接器识别
        mappedData.put("_DATA_TYPE", dataType);

        // 获取连接器
        SystemConnector connector = connectorFactory.getConnector(config);

        // 创建日志记录
        LogDistribution log = createLog(dataType, dataId, config, sourceData, mappedData);

        DistributionResultDTO result;
        try {
            log.setStatus("RUNNING");
            log.setStartTime(LocalDateTime.now());
            logDistributionRepository.save(log);

            // 判断是创建还是更新
            // 优先尝试更新，如果不存在再创建
            String targetKey = getDataCode(dataType, sourceData);
            result = connector.update(targetKey, mappedData);

            if (result.isSuccess()) {
                // 更新成功
                log.setOperation("UPDATE");
            } else {
                // 更新失败，尝试创建
                logger.info("更新失败，尝试创建: {}", result.getErrorMsg());
                result = connector.create(mappedData);
                if (result.isSuccess()) {
                    log.setOperation("CREATE");
                }
            }

            // 根据结果更新日志
            if (result.isSuccess()) {
                updateLogSuccess(log, result);
            } else {
                updateLogFailed(log, result);
            }

            // 无论成功失败，都保存字段级血缘数据（用于追踪）
            try {
                lineageService.saveFieldLineage(log, sourceData, mappedData, mappings);
            } catch (Exception e) {
                logger.warn("保存字段血缘失败: {}", e.getMessage());
            }

        } catch (Exception e) {
            logger.error("分发失败: {}", e.getMessage());
            result = DistributionResultDTO.fail("DISTRIBUTE_ERROR", e.getMessage());
            updateLogFailed(log, result);
        }

        return result;
    }

    /**
     * 批量分发
     */
    @Transactional
    public List<DistributionResultDTO> batchDistribute(String dataType, List<Map<String, Object>> dataList, Long systemConfigId) {
        List<DistributionResultDTO> results = new ArrayList<>();

        for (Map<String, Object> data : dataList) {
            Long dataId = getLongValue(data, "id");
            try {
                DistributionResultDTO result = distribute(dataType, dataId, data, systemConfigId);
                results.add(result);
            } catch (Exception e) {
                results.add(DistributionResultDTO.fail(e.getMessage()));
            }
        }

        return results;
    }

    /**
     * 重试分发
     */
    @Transactional
    public DistributionResultDTO retry(Long logId) {
        LogDistribution oldLog = logDistributionRepository.findById(logId)
                .orElseThrow(() -> new BusinessException("日志记录不存在"));

        if (!"FAILED".equals(oldLog.getStatus())) {
            throw new BusinessException("只有失败的记录才能重试");
        }

        // 解析原始请求数据
        Map<String, Object> sourceData = parseJson(oldLog.getRequestData());

        // 执行分发
        DistributionResultDTO result = distribute(
                oldLog.getDataType(),
                oldLog.getDataId(),
                sourceData,
                oldLog.getSystemConfigId()
        );

        // 更新重试次数
        oldLog.setRetryCount(oldLog.getRetryCount() + 1);
        logDistributionRepository.save(oldLog);

        return result;
    }

    /**
     * 字段映射转换
     */
    private Map<String, Object> mapFields(Map<String, Object> sourceData, List<FieldMapping> mappings) {
        Map<String, Object> result = new HashMap<>();

        for (FieldMapping mapping : mappings) {
            String mdmField = mapping.getMdmField();
            String sapField = mapping.getSapField();
            Object value = sourceData.get(mdmField);

            if (value == null && mapping.getDefaultValue() != null) {
                value = mapping.getDefaultValue();
            }

            if (value != null) {
                // 根据转换类型处理
                String transformType = mapping.getTransformType();
                value = transformValue(value, transformType, mapping.getTransformRule());

                result.put(sapField, value);
            }
        }

        return result;
    }

    /**
     * 值转换
     */
    private Object transformValue(Object value, String transformType, String transformRule) {
        if ("DIRECT".equals(transformType) || transformRule == null) {
            return value;
        }

        try {
            switch (transformType) {
                case "VALUE_MAP":
                    // 值域映射
                    @SuppressWarnings("unchecked")
                    Map<String, Object> valueMap = objectMapper.readValue(transformRule, Map.class);
                    Object mapped = valueMap.get(value.toString());
                    return mapped != null ? mapped : value;

                case "FIXED":
                    // 固定值
                    return transformRule;

                case "EXPRESSION":
                    // 表达式（简单实现）
                    return value.toString() + transformRule;

                default:
                    return value;
            }
        } catch (Exception e) {
            logger.warn("值转换失败: {}", e.getMessage());
            return value;
        }
    }

    /**
     * 创建日志记录
     */
    private LogDistribution createLog(String dataType, Long dataId, DisSystemConfig config,
                                       Map<String, Object> sourceData, Map<String, Object> mappedData) {
        LogDistribution log = new LogDistribution();
        log.setLogCode(generateLogCode());
        log.setDataType(dataType);
        log.setDataTypeName(getDataTypeName(dataType));
        log.setDataId(dataId);
        // 根据数据类型获取正确的编码和名称字段
        log.setDataCode(getDataCode(dataType, sourceData));
        log.setDataName(getDataName(dataType, sourceData));
        log.setSystemConfigId(config.getId());
        log.setSystemConfigName(config.getConfigName());
        log.setSystemType(config.getSystemType());
        log.setInterfaceName(config.getSystemType());
        log.setStatus("PENDING");
        log.setRequestData(toJson(sourceData));
        log.setMappedData(toJson(mappedData));

        return log;
    }

    /**
     * 根据数据类型获取表单名称
     */
    private String getDataTypeName(String dataType) {
        if (dataType == null) return "";
        switch (dataType) {
            case "VENDOR":
                return "供应商";
            case "MATERIAL":
                return "物料";
            case "CUSTOMER":
                return "客户";
            case "EMPLOYEE":
                return "员工";
            case "ORGANIZATION":
                return "组织";
            default:
                return dataType;
        }
    }

    /**
     * 根据数据类型获取数据编码
     */
    private String getDataCode(String dataType, Map<String, Object> data) {
        // 优先尝试通用字段名
        String code = getStringValue(data, "code");
        if (code != null) return code;

        // 根据数据类型获取特定字段
        switch (dataType) {
            case "MATERIAL":
                return getStringValue(data, "MATNR");
            case "CUSTOMER":
            case "VENDOR":
                return getStringValue(data, "PARTNER_CODE");
            default:
                return null;
        }
    }

    /**
     * 根据数据类型获取数据名称
     */
    private String getDataName(String dataType, Map<String, Object> data) {
        // 优先尝试通用字段名
        String name = getStringValue(data, "name");
        if (name != null) return name;

        // 根据数据类型获取特定字段
        switch (dataType) {
            case "MATERIAL":
                return getStringValue(data, "MAKTX");
            case "CUSTOMER":
            case "VENDOR":
                return getStringValue(data, "PARTNER_NAME");
            default:
                return null;
        }
    }

    /**
     * 更新日志 - 成功
     */
    private void updateLogSuccess(LogDistribution log, DistributionResultDTO result) {
        log.setStatus("SUCCESS");
        log.setEndTime(LocalDateTime.now());
        log.setDurationMs(calculateDuration(log.getStartTime(), log.getEndTime()));
        log.setSapKey(result.getTargetKey());
        log.setSapReturnCode(result.getSapReturnCode());
        log.setSapMessageType(result.getSapMessageType());
        log.setSapMessage(result.getMessage());
        log.setResponseData(toJson(result.getResponseData()));
        logDistributionRepository.save(log);
    }

    /**
     * 更新日志 - 失败
     */
    private void updateLogFailed(LogDistribution log, DistributionResultDTO result) {
        log.setStatus("FAILED");
        log.setEndTime(LocalDateTime.now());
        log.setDurationMs(calculateDuration(log.getStartTime(), log.getEndTime()));
        log.setErrorCode(result.getErrorCode());
        log.setErrorMsg(result.getErrorMsg());
        log.setSapMessageType(result.getSapMessageType());
        log.setSapReturnCode(result.getSapReturnCode());
        log.setSapMessage(result.getMessage());
        log.setResponseData(toJson(result.getResponseData()));
        logDistributionRepository.save(log);
    }

    /**
     * 获取最后分发状态
     */
    private String getLastDistributeStatus(String dataType, Long dataId, Long systemConfigId) {
        List<LogDistribution> logs = logDistributionRepository.findByDataTypeAndDataIdOrderByCreatedDesc(dataType, dataId);
        return logs.isEmpty() ? null : logs.get(0).getStatus();
    }

    /**
     * 获取最后目标Key
     */
    private String getLastTargetKey(String dataType, Long dataId, Long systemConfigId) {
        List<LogDistribution> logs = logDistributionRepository.findByDataTypeAndDataIdOrderByCreatedDesc(dataType, dataId);
        if (!logs.isEmpty()) {
            return logs.get(0).getSapKey();
        }
        return null;
    }

    /**
     * 生成日志编码
     */
    private String generateLogCode() {
        return "DIS" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
    }

    private Integer calculateDuration(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return null;
        return (int) (java.time.Duration.between(start, end).toMillis());
    }

    private String toJson(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJson(String json) {
        if (json == null) return new HashMap<>();
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    private String getStringValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        return value != null ? value.toString() : null;
    }

    private Long getLongValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value == null) return null;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Number) return ((Number) value).longValue();
        try {
            return Long.parseLong(value.toString());
        } catch (Exception e) {
            return null;
        }
    }
}

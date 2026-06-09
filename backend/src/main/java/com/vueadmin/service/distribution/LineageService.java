package com.vueadmin.service.distribution;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vueadmin.dto.distribution.FieldLineageFlatDTO;
import com.vueadmin.dto.distribution.LineageDetailDTO;
import com.vueadmin.dto.distribution.LineageSummaryDTO;
import com.vueadmin.entity.distribution.FieldLineage;
import com.vueadmin.entity.distribution.FieldMapping;
import com.vueadmin.entity.distribution.LogDistribution;
import com.vueadmin.exception.BusinessException;
import com.vueadmin.repository.FieldLineageRepository;
import com.vueadmin.repository.FieldMappingRepository;
import com.vueadmin.repository.LogDistributionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 血缘分析服务
 */
@Service
public class LineageService {

    private static final Logger logger = LoggerFactory.getLogger(LineageService.class);

    @Autowired
    private LogDistributionRepository logDistributionRepository;

    @Autowired
    private FieldLineageRepository fieldLineageRepository;

    @Autowired
    private FieldMappingRepository fieldMappingRepository;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 血缘分析搜索
     */
    public Page<LineageSummaryDTO> searchLineage(String dataType, String status,
                                                   Long systemConfigId, LocalDateTime startTime,
                                                   LocalDateTime endTime, Pageable pageable) {
        Page<LogDistribution> logs = logDistributionRepository.searchLogs(
                dataType, status, systemConfigId, startTime, endTime, pageable);

        List<LineageSummaryDTO> summaries = logs.getContent().stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(summaries, pageable, logs.getTotalElements());
    }

    /**
     * 获取血缘详情
     */
    public LineageDetailDTO getLineageDetail(Long logId) {
        LogDistribution log = logDistributionRepository.findById(logId)
                .orElseThrow(() -> new BusinessException("日志不存在"));

        return convertToDetailDTO(log);
    }

    /**
     * 获取数据的完整血缘（包含所有分发记录）
     */
    public List<LineageDetailDTO> getDataLineage(String dataType, Long dataId) {
        List<LogDistribution> logs = logDistributionRepository.findByDataTypeAndDataIdOrderByCreatedDesc(dataType, dataId);

        return logs.stream()
                .map(this::convertToDetailDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取字段级血缘列表
     */
    public List<LineageDetailDTO.FieldLineageDTO> getFieldLineage(Long logId) {
        List<FieldLineage> fieldLineages = fieldLineageRepository.findByLogIdOrderById(logId);

        return fieldLineages.stream()
                .map(this::convertToFieldLineageDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取扁平化的字段级血缘列表（用于直接展示）
     */
    public Page<FieldLineageFlatDTO> getFieldLineageFlatList(String dataType, String status,
                                                               Long systemConfigId, LocalDateTime startTime,
                                                               LocalDateTime endTime, Pageable pageable) {
        // 先查询分发日志（按开始时间降序）
        PageRequest logPageRequest = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "startTime")
        );

        Page<LogDistribution> logs = logDistributionRepository.searchLogs(
                dataType, status, systemConfigId, startTime, endTime, logPageRequest);

        // 转换为扁平化的字段级血缘
        List<FieldLineageFlatDTO> flatList = new ArrayList<>();

        for (LogDistribution log : logs.getContent()) {
            // 获取该日志的字段级血缘
            List<FieldLineage> fieldLineages = fieldLineageRepository.findByLogIdOrderById(log.getId());

            for (FieldLineage fl : fieldLineages) {
                FieldLineageFlatDTO dto = new FieldLineageFlatDTO();
                // 日志信息
                dto.setLogId(log.getId());
                dto.setLogCode(log.getLogCode());

                // 数据信息
                dto.setDataId(log.getDataId());
                dto.setDataCode(log.getDataCode());
                dto.setDataName(log.getDataName());
                dto.setDataType(log.getDataType());
                dto.setFormName(getFormName(log.getDataType()));

                // 系统信息
                dto.setSystemConfigName(log.getSystemConfigName());

                // 字段信息
                dto.setMdmField(fl.getMdmField());
                dto.setMdmFieldName(fl.getMdmFieldName());
                dto.setSapField(fl.getSapField());
                dto.setSapFieldName(fl.getSapFieldName());

                // 字段值
                dto.setSourceValue(fl.getSourceValue());
                dto.setTargetValue(fl.getTargetValue());

                // 转换信息
                dto.setTransformType(fl.getTransformType());
                dto.setTransformTypeName(getTransformTypeName(fl.getTransformType()));
                dto.setTransformRule(fl.getTransformRule());

                // 状态
                dto.setFieldStatus(fl.getStatus());
                dto.setFieldStatusName(getFieldStatusName(fl.getStatus()));

                // 时间
                dto.setSendTime(log.getStartTime());
                dto.setReceiveTime(log.getEndTime());

                // 操作信息
                dto.setOperation(log.getOperation());
                dto.setOperationName(getOperationName(log.getOperation()));

                // 整体状态
                dto.setOverallStatus(log.getStatus());
                dto.setOverallStatusName(getStatusName(log.getStatus()));

                flatList.add(dto);
            }
        }

        // 按发送时间降序排序
        flatList.sort((a, b) -> {
            if (a.getSendTime() == null && b.getSendTime() == null) return 0;
            if (a.getSendTime() == null) return 1;
            if (b.getSendTime() == null) return -1;
            return b.getSendTime().compareTo(a.getSendTime());
        });

        return new PageImpl<>(flatList, pageable, logs.getTotalElements() * 10); // 估算总数
    }

    /**
     * 保存字段级血缘
     */
    @Transactional
    public void saveFieldLineage(LogDistribution log,
                                  Map<String, Object> sourceData,
                                  Map<String, Object> mappedData,
                                  List<FieldMapping> mappings) {
        List<FieldLineage> fieldLineages = new ArrayList<>();
        int successCount = 0;
        int failedCount = 0;

        for (FieldMapping mapping : mappings) {
            FieldLineage fl = new FieldLineage();
            fl.setLogId(log.getId());
            fl.setDataType(log.getDataType());
            fl.setDataId(log.getDataId());
            fl.setMdmField(mapping.getMdmField());
            fl.setMdmFieldName(mapping.getMdmFieldName());
            fl.setSapField(mapping.getSapField());
            fl.setSapFieldName(mapping.getSapFieldName());

            // 获取源值
            Object sourceValue = sourceData.get(mapping.getMdmField());
            fl.setSourceValue(sourceValue != null ? sourceValue.toString() : null);

            // 获取目标值
            Object targetValue = mappedData.get(mapping.getSapField());
            fl.setTargetValue(targetValue != null ? targetValue.toString() : null);

            fl.setTransformType(mapping.getTransformType());
            fl.setTransformRule(mapping.getTransformRule());

            // 判断字段状态
            String fieldStatus = determineFieldStatus(log, mapping, targetValue);
            fl.setStatus(fieldStatus);

            // 统计成功失败数量
            if ("SUCCESS".equals(fieldStatus)) {
                successCount++;
            } else {
                failedCount++;
                // 如果有SAP错误消息，记录到字段
                if (log.getErrorMsg() != null && log.getErrorMsg().contains(mapping.getSapField())) {
                    fl.setErrorMsg(extractFieldError(log.getErrorMsg(), mapping.getSapField()));
                }
            }

            fieldLineages.add(fl);
        }

        fieldLineageRepository.saveAll(fieldLineages);

        // 更新日志的字段统计
        log.setFieldCount(fieldLineages.size());
        log.setSuccessFieldCount(successCount);
        logDistributionRepository.save(log);
    }

    /**
     * 判断字段状态
     */
    private String determineFieldStatus(LogDistribution log, FieldMapping mapping, Object targetValue) {
        // 如果分发整体成功，字段也成功
        if ("SUCCESS".equals(log.getStatus())) {
            return "SUCCESS";
        }

        // 如果分发失败，检查是否是此字段导致的错误
        String errorMsg = log.getErrorMsg();
        String sapMessage = log.getSapMessage();
        String combinedMsg = (errorMsg != null ? errorMsg : "") + " " + (sapMessage != null ? sapMessage : "");

        if (!combinedMsg.trim().isEmpty()) {
            // 检查错误消息中是否包含此字段（字段名或字段中文名）
            String sapField = mapping.getSapField();
            String mdmField = mapping.getMdmField();
            String sapFieldName = mapping.getSapFieldName();
            String mdmFieldName = mapping.getMdmFieldName();

            if (combinedMsg.contains(sapField) ||
                combinedMsg.contains(mdmField) ||
                (sapFieldName != null && combinedMsg.contains(sapFieldName)) ||
                (mdmFieldName != null && combinedMsg.contains(mdmFieldName))) {
                return "FAILED";
            }

            // 特殊字段匹配（计量单位相关）
            if (isUnitField(sapField, mdmField) &&
                (combinedMsg.contains("计量单位") || combinedMsg.contains("单位"))) {
                return "FAILED";
            }

            // 物料组相关
            if (isMaterialGroupField(sapField, mdmField) &&
                (combinedMsg.contains("物料组") || combinedMsg.contains("MATKL"))) {
                return "FAILED";
            }
        }

        // 如果目标值为空且字段是必填的，标记为失败
        if (mapping.getIsRequired() != null && mapping.getIsRequired() == 1 && targetValue == null) {
            return "FAILED";
        }

        // 如果分发失败但不是此字段的问题，标记为成功
        return "SUCCESS";
    }

    /**
     * 判断是否是计量单位相关字段
     */
    private boolean isUnitField(String sapField, String mdmField) {
        return "MEINS".equals(sapField) || "BASE_UOM".equals(sapField) ||
               "MEINS".equals(mdmField) || "BASE_UOM".equals(mdmField);
    }

    /**
     * 判断是否是物料组相关字段
     */
    private boolean isMaterialGroupField(String sapField, String mdmField) {
        return "MATKL".equals(sapField) || "MATL_GROUP".equals(sapField) ||
               "MATKL".equals(mdmField) || "MATL_GROUP".equals(mdmField);
    }

    /**
     * 提取字段错误信息
     */
    private String extractFieldError(String errorMsg, String fieldName) {
        if (errorMsg == null) return null;

        // 尝试提取与字段相关的错误信息
        try {
            int idx = errorMsg.indexOf(fieldName);
            if (idx > 0) {
                // 提取字段相关的错误描述
                int start = Math.max(0, idx - 50);
                int end = Math.min(errorMsg.length(), idx + fieldName.length() + 100);
                return errorMsg.substring(start, end).trim();
            }
        } catch (Exception e) {
            // 忽略异常
        }

        return null;
    }

    /**
     * 转换为汇总DTO
     */
    private LineageSummaryDTO convertToSummaryDTO(LogDistribution log) {
        LineageSummaryDTO dto = new LineageSummaryDTO();
        dto.setLogId(log.getId());
        dto.setLogCode(log.getLogCode());
        dto.setDataId(log.getDataId());
        dto.setDataCode(log.getDataCode());
        dto.setDataName(log.getDataName());
        dto.setDataType(log.getDataType());
        dto.setFormName(getFormName(log.getDataType()));
        dto.setSystemConfigId(log.getSystemConfigId());
        dto.setSystemConfigName(log.getSystemConfigName());
        dto.setSystemType(log.getSystemType());
        dto.setInterfaceName(log.getInterfaceName());
        dto.setOperation(log.getOperation());
        dto.setOperationName(getOperationName(log.getOperation()));
        dto.setSendStatus(log.getStatus());
        dto.setStatusName(getStatusName(log.getStatus()));
        dto.setSendTime(log.getStartTime());
        dto.setReceiveTime(log.getEndTime());
        dto.setDurationMs(log.getDurationMs());
        dto.setTargetKey(log.getSapKey());
        dto.setFieldCount(log.getFieldCount() != null ? log.getFieldCount() : 0);
        dto.setSuccessFieldCount(log.getSuccessFieldCount() != null ? log.getSuccessFieldCount() : 0);
        dto.setFailedFieldCount(dto.getFieldCount() - dto.getSuccessFieldCount());
        dto.setErrorCode(log.getErrorCode());
        dto.setErrorMsg(log.getErrorMsg());
        dto.setCreatedBy(log.getCreatedBy());
        dto.setCreatedAt(log.getCreatedAt());

        return dto;
    }

    /**
     * 转换为详情DTO
     */
    private LineageDetailDTO convertToDetailDTO(LogDistribution log) {
        LineageDetailDTO dto = new LineageDetailDTO();
        dto.setLogId(log.getId());
        dto.setLogCode(log.getLogCode());
        dto.setDataId(log.getDataId());
        dto.setDataCode(log.getDataCode());
        dto.setDataName(log.getDataName());
        dto.setDataType(log.getDataType());
        dto.setFormName(getFormName(log.getDataType()));
        dto.setSystemConfigId(log.getSystemConfigId());
        dto.setSystemConfigName(log.getSystemConfigName());
        dto.setSystemType(log.getSystemType());
        dto.setInterfaceName(log.getInterfaceName());
        dto.setOperation(log.getOperation());
        dto.setOperationName(getOperationName(log.getOperation()));
        dto.setSendStatus(log.getStatus());
        dto.setSendTime(log.getStartTime());
        dto.setReceiveTime(log.getEndTime());
        dto.setDurationMs(log.getDurationMs());
        dto.setTargetKey(log.getSapKey());
        dto.setFieldCount(log.getFieldCount() != null ? log.getFieldCount() : 0);
        dto.setSuccessFieldCount(log.getSuccessFieldCount() != null ? log.getSuccessFieldCount() : 0);
        dto.setSapReturnCode(log.getSapReturnCode());
        dto.setSapMessageType(log.getSapMessageType());
        dto.setSapMessage(log.getSapMessage());
        dto.setErrorCode(log.getErrorCode());
        dto.setErrorMsg(log.getErrorMsg());
        dto.setCreatedBy(log.getCreatedBy());
        dto.setCreatedAt(log.getCreatedAt());

        // 获取字段级血缘
        List<FieldLineage> fieldLineages = fieldLineageRepository.findByLogIdOrderById(log.getId());
        List<LineageDetailDTO.FieldLineageDTO> fieldDTOs = fieldLineages.stream()
                .map(this::convertToFieldLineageDTO)
                .collect(Collectors.toList());
        dto.setFieldLineages(fieldDTOs);

        return dto;
    }

    /**
     * 转换为字段级血缘DTO
     */
    private LineageDetailDTO.FieldLineageDTO convertToFieldLineageDTO(FieldLineage fl) {
        LineageDetailDTO.FieldLineageDTO dto = new LineageDetailDTO.FieldLineageDTO();
        dto.setId(fl.getId());
        dto.setMdmField(fl.getMdmField());
        dto.setMdmFieldName(fl.getMdmFieldName());
        dto.setSapField(fl.getSapField());
        dto.setSapFieldName(fl.getSapFieldName());
        dto.setSourceValue(fl.getSourceValue());
        dto.setTargetValue(fl.getTargetValue());
        dto.setTransformType(fl.getTransformType());
        dto.setTransformTypeName(getTransformTypeName(fl.getTransformType()));
        dto.setTransformRule(fl.getTransformRule());
        dto.setStatus(fl.getStatus());
        dto.setStatusName(getFieldStatusName(fl.getStatus()));
        dto.setErrorMsg(fl.getErrorMsg());

        return dto;
    }

    /**
     * 获取表单名
     */
    private String getFormName(String dataType) {
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
     * 获取操作名称
     */
    private String getOperationName(String operation) {
        if (operation == null) return "";
        switch (operation) {
            case "CREATE":
                return "新增";
            case "UPDATE":
                return "更新";
            case "DELETE":
                return "删除";
            default:
                return operation;
        }
    }

    /**
     * 获取状态名称
     */
    private String getStatusName(String status) {
        if (status == null) return "";
        switch (status) {
            case "PENDING":
                return "待执行";
            case "RUNNING":
                return "执行中";
            case "SUCCESS":
                return "成功";
            case "FAILED":
                return "失败";
            default:
                return status;
        }
    }

    /**
     * 获取转换类型名称
     */
    private String getTransformTypeName(String transformType) {
        if (transformType == null) return "直接映射";
        switch (transformType) {
            case "DIRECT":
                return "直接映射";
            case "VALUE_MAP":
                return "值域映射";
            case "FIXED":
                return "固定值";
            case "EXPRESSION":
                return "表达式计算";
            default:
                return transformType;
        }
    }

    /**
     * 获取字段状态名称
     */
    private String getFieldStatusName(String status) {
        if (status == null) return "";
        switch (status) {
            case "SUCCESS":
                return "成功";
            case "FAILED":
                return "失败";
            case "SKIPPED":
                return "跳过";
            default:
                return status;
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
}

package com.vueadmin.service.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vueadmin.dto.DataOperationLogDto;
import com.vueadmin.dto.ViewDefinitionDto;
import com.vueadmin.dto.ViewEntityDto;
import com.vueadmin.dto.ViewFieldDto;
import com.vueadmin.entity.data.DataCategory;
import com.vueadmin.entity.data.DataOperationLog;
import com.vueadmin.entity.form.Form;
import com.vueadmin.exception.BusinessException;
import com.vueadmin.repository.DataCategoryRepository;
import com.vueadmin.repository.DataOperationLogRepository;
import com.vueadmin.repository.FormRepository;
import com.vueadmin.service.standard.ViewDefinitionService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据操作日志Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataOperationLogService {

    private final DataOperationLogRepository logRepository;
    private final DataCategoryRepository categoryRepository;
    private final FormRepository formRepository;
    private final ViewDefinitionService viewDefinitionService;
    private final EntityManager entityManager;
    private final ObjectMapper objectMapper;

    /**
     * 查询日志列表
     */
    public List<DataOperationLogDto> getList(Long categoryId, Long formId) {
        List<DataOperationLog> logs;

        if (categoryId != null && formId != null) {
            logs = logRepository.findByCategoryIdAndFormId(categoryId, formId);
        } else if (formId != null) {
            logs = logRepository.findByFormId(formId);
        } else if (categoryId != null) {
            logs = logRepository.findByCategoryId(categoryId);
        } else {
            logs = logRepository.findAll();
        }

        return logs.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 获取日志详情
     */
    public DataOperationLogDto getById(Long id) {
        DataOperationLog logEntity = logRepository.findById(id)
                .orElseThrow(() -> new BusinessException("日志不存在"));
        return convertToDto(logEntity);
    }

    /**
     * 根据记录ID查询操作日志
     */
    public List<DataOperationLogDto> getLogsByRecordId(Long formId, Long recordId) {
        List<DataOperationLog> logs = logRepository.findByFormIdAndMainRecordIdOrderByCreatedAtDesc(formId, recordId);
        return logs.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 记录创建操作日志（详细记录）
     */
    @Transactional
    public void logCreateOperation(Long categoryId, Long formId, Long mainRecordId,
                                   String mainTableName, Map<String, Object> data, String createdBy) {
        try {
            // 获取视图定义以获取字段名称和子表名称
            Map<String, String> fieldNames = getFieldNames(formId);
            Map<String, String> subEntityNames = getSubEntityNames(formId);

            // 构建详细的主表数据
            Map<String, Object> mainData = new HashMap<>(data);
            mainData.remove("sub");

            Map<String, Object> mainTableData = new HashMap<>();
            for (Map.Entry<String, Object> entry : mainData.entrySet()) {
                String fieldCode = entry.getKey();
                Object value = entry.getValue();
                Map<String, Object> fieldInfo = new HashMap<>();
                fieldInfo.put("value", value);
                fieldInfo.put("fieldName", fieldNames.getOrDefault(fieldCode, fieldCode));
                mainTableData.put(fieldCode, fieldInfo);
            }

            // 构建摘要信息
            Map<String, Object> summary = new HashMap<>();
            summary.put("operation", "create");
            summary.put("mainRecordId", mainRecordId);
            summary.put("mainTable", mainTableName);
            summary.put("mainTableData", mainTableData);
            summary.put("mainTableFieldCount", mainData.size());

            // 统计子表数据（使用中文名称）
            @SuppressWarnings("unchecked")
            Map<String, Object> subData = (Map<String, Object>) data.get("sub");
            if (subData != null && !subData.isEmpty()) {
                Map<String, Object> subTableDetails = new HashMap<>();
                for (Map.Entry<String, Object> entry : subData.entrySet()) {
                    if (entry.getValue() instanceof List) {
                        List<?> rows = (List<?>) entry.getValue();
                        String entityCode = entry.getKey();
                        Map<String, Object> subInfo = new HashMap<>();
                        subInfo.put("rowCount", rows.size());
                        subInfo.put("entityCode", entityCode);
                        subInfo.put("entityName", subEntityNames.getOrDefault(entityCode, entityCode));
                        subTableDetails.put(entityCode, subInfo);
                    }
                }
                summary.put("subTableDetails", subTableDetails);
            }

            // 保存日志
            DataOperationLog logEntity = new DataOperationLog();
            logEntity.setCategoryId(categoryId);
            logEntity.setFormId(formId);
            Form form = formRepository.findById(formId).orElse(null);
            logEntity.setViewId(form != null ? form.getViewId() : null);
            logEntity.setOperationType("create");
            logEntity.setOperationDetail("创建数据");
            logEntity.setFromStatus(null);
            logEntity.setToStatus("DRAFT");
            logEntity.setMainRecordId(mainRecordId);
            logEntity.setOperationData(objectMapper.writeValueAsString(summary));
            logEntity.setStatus("active");
            logEntity.setCreatedBy(createdBy);
            logEntity.setCreatedAt(LocalDateTime.now());

            logRepository.save(logEntity);
            log.info("记录创建操作日志: formId={}, recordId={}, user={}", formId, mainRecordId, createdBy);
        } catch (JsonProcessingException e) {
            log.error("记录创建操作日志失败", e);
        }
    }

    /**
     * 获取字段名称映射
     */
    private Map<String, String> getFieldNames(Long formId) {
        Map<String, String> fieldNames = new HashMap<>();
        try {
            Form form = formRepository.findById(formId).orElse(null);
            if (form != null && form.getViewId() != null) {
                ViewDefinitionDto viewDef = viewDefinitionService.getViewDetail(form.getViewId());
                if (viewDef != null && viewDef.getEntities() != null) {
                    for (ViewEntityDto entity : viewDef.getEntities()) {
                        if (entity.getFields() != null) {
                            for (ViewFieldDto field : entity.getFields()) {
                                fieldNames.put(field.getFieldCode(), field.getFieldName());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("获取字段名称失败: {}", e.getMessage());
        }
        return fieldNames;
    }

    /**
     * 获取子表实体名称映射（entityCode -> entityName）
     */
    private Map<String, String> getSubEntityNames(Long formId) {
        Map<String, String> entityNames = new HashMap<>();
        try {
            Form form = formRepository.findById(formId).orElse(null);
            if (form != null && form.getViewId() != null) {
                ViewDefinitionDto viewDef = viewDefinitionService.getViewDetail(form.getViewId());
                if (viewDef != null && viewDef.getEntities() != null) {
                    for (ViewEntityDto entity : viewDef.getEntities()) {
                        if ("sub".equals(entity.getEntityType())) {
                            entityNames.put(entity.getEntityCode(), entity.getEntityName());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("获取子表实体名称失败: {}", e.getMessage());
        }
        return entityNames;
    }

    /**
     * 记录更新操作日志（简化记录，记录变更字段名称）
     */
    @Transactional
    public void logUpdateOperation(Long categoryId, Long formId, Long mainRecordId,
                                   Map<String, Object> changes, String createdBy) {
        try {
            // 获取字段名称映射
            Map<String, String> fieldNames = getFieldNames(formId);
            Map<String, String> subEntityNames = getSubEntityNames(formId);

            // 构建简化变更信息
            Map<String, Object> summary = new HashMap<>();
            summary.put("operation", "update");
            summary.put("mainRecordId", mainRecordId);

            // 统计变更数量并记录字段名称
            int changeCount = 0;
            if (changes != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> mainChanges = (Map<String, Object>) changes.get("mainTable");
                if (mainChanges != null) {
                    changeCount += mainChanges.size();
                    // 记录变更的字段名称（转换为中文名）
                    Map<String, Object> mainChangeDetails = new HashMap<>();
                    for (Map.Entry<String, Object> entry : mainChanges.entrySet()) {
                        String fieldCode = entry.getKey();
                        String fieldName = fieldNames.getOrDefault(fieldCode, fieldCode);
                        Map<String, Object> changeInfo = new HashMap<>();
                        changeInfo.put("fieldName", fieldName);
                        if (entry.getValue() instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> valueChange = (Map<String, Object>) entry.getValue();
                            changeInfo.put("oldValue", valueChange.get("oldValue"));
                            changeInfo.put("newValue", valueChange.get("newValue"));
                        }
                        mainChangeDetails.put(fieldCode, changeInfo);
                    }
                    summary.put("mainTable", mainChangeDetails);
                }

                @SuppressWarnings("unchecked")
                Map<String, Object> subChanges = (Map<String, Object>) changes.get("subTables");
                if (subChanges != null) {
                    changeCount += subChanges.size();
                    // 记录子表变更
                    Map<String, Object> subChangeDetails = new HashMap<>();
                    for (Map.Entry<String, Object> entry : subChanges.entrySet()) {
                        String entityCode = entry.getKey();
                        String entityName = subEntityNames.getOrDefault(entityCode, entityCode);
                        Map<String, Object> subInfo = new HashMap<>();
                        subInfo.put("entityName", entityName);
                        if (entry.getValue() instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> subChange = (Map<String, Object>) entry.getValue();
                            if (subChange.containsKey("oldCount") && subChange.containsKey("newCount")) {
                                subInfo.put("oldCount", subChange.get("oldCount"));
                                subInfo.put("newCount", subChange.get("newCount"));
                                subInfo.put("countChanged", true);
                            }
                        }
                        subChangeDetails.put(entityCode, subInfo);
                    }
                    summary.put("subTables", subChangeDetails);
                }
            }
            summary.put("changeCount", changeCount);

            // 保存日志
            DataOperationLog logEntity = new DataOperationLog();
            logEntity.setCategoryId(categoryId);
            logEntity.setFormId(formId);
            Form form = formRepository.findById(formId).orElse(null);
            logEntity.setViewId(form != null ? form.getViewId() : null);
            logEntity.setOperationType("update");
            logEntity.setOperationDetail("更新数据");
            logEntity.setMainRecordId(mainRecordId);
            logEntity.setOperationData(objectMapper.writeValueAsString(summary));
            logEntity.setStatus("active");
            logEntity.setCreatedBy(createdBy);
            logEntity.setCreatedAt(LocalDateTime.now());

            logRepository.save(logEntity);
            log.info("记录更新操作日志: formId={}, recordId={}, user={}, 变更{}个字段", formId, mainRecordId, createdBy, changeCount);
        } catch (JsonProcessingException e) {
            log.error("记录更新操作日志失败", e);
        }
    }

    /**
     * 记录删除操作日志（存储完整快照）
     */
    @Transactional
    public void logDeleteOperation(Long categoryId, Long formId, Long mainRecordId,
                                   Map<String, Object> deletedData, String createdBy) {
        try {
            // 构建完整快照
            Map<String, Object> snapshot = new HashMap<>();
            snapshot.put("operation", "delete");
            snapshot.put("mainRecordId", mainRecordId);
            snapshot.put("snapshot", deletedData);
            snapshot.put("deletedAt", LocalDateTime.now().toString());
            snapshot.put("deletedBy", createdBy);

            // 保存日志
            DataOperationLog logEntity = new DataOperationLog();
            logEntity.setCategoryId(categoryId);
            logEntity.setFormId(formId);
            Form form = formRepository.findById(formId).orElse(null);
            logEntity.setViewId(form != null ? form.getViewId() : null);
            logEntity.setOperationType("delete");
            logEntity.setOperationDetail("删除数据");
            logEntity.setMainRecordId(mainRecordId);
            logEntity.setOperationData(objectMapper.writeValueAsString(snapshot));
            logEntity.setStatus("active");
            logEntity.setCreatedBy(createdBy);
            logEntity.setCreatedAt(LocalDateTime.now());

            logRepository.save(logEntity);
        } catch (JsonProcessingException e) {
            log.error("记录删除操作日志失败", e);
        }
    }

    /**
     * 记录操作日志（兼容旧方法，标记为废弃）
     */
    @Deprecated
    @Transactional
    public void logOperation(Long categoryId, Long formId, Long mainRecordId,
                            String operationType, Object data, String createdBy) {
        try {
            Form form = formRepository.findById(formId).orElse(null);

            DataOperationLog logEntity = new DataOperationLog();
            logEntity.setCategoryId(categoryId);
            logEntity.setFormId(formId);
            logEntity.setViewId(form != null ? form.getViewId() : null);
            logEntity.setOperationType(operationType);
            logEntity.setMainRecordId(mainRecordId);
            logEntity.setOperationData(objectMapper.writeValueAsString(data));
            logEntity.setStatus("active");
            logEntity.setCreatedBy(createdBy);
            logEntity.setCreatedAt(LocalDateTime.now());

            logRepository.save(logEntity);
        } catch (JsonProcessingException e) {
            log.error("记录操作日志失败", e);
        }
    }

    /**
     * 转换为DTO
     */
    private DataOperationLogDto convertToDto(DataOperationLog logEntity) {
        DataOperationLogDto dto = new DataOperationLogDto();
        dto.setId(logEntity.getId());
        dto.setCategoryId(logEntity.getCategoryId());
        dto.setFormId(logEntity.getFormId());
        dto.setViewId(logEntity.getViewId());
        dto.setOperationType(logEntity.getOperationType());
        dto.setOperationDetail(logEntity.getOperationDetail());
        dto.setFromStatus(logEntity.getFromStatus());
        dto.setToStatus(logEntity.getToStatus());
        dto.setMainRecordId(logEntity.getMainRecordId());
        dto.setOperationData(logEntity.getOperationData());
        dto.setQualityScore(logEntity.getQualityScore());
        dto.setQualityIssues(logEntity.getQualityIssues());
        dto.setOperationReason(logEntity.getOperationReason());
        dto.setStatus(logEntity.getStatus());
        dto.setCreatedBy(getUserName(logEntity.getCreatedBy()));
        dto.setUpdatedBy(logEntity.getUpdatedBy());
        dto.setIpAddress(logEntity.getIpAddress());
        dto.setUserAgent(logEntity.getUserAgent());

        if (logEntity.getCreatedAt() != null) {
            dto.setCreatedAt(logEntity.getCreatedAt().toString());
        }
        if (logEntity.getUpdatedAt() != null) {
            dto.setUpdatedAt(logEntity.getUpdatedAt().toString());
        }

        // 获取表单名称
        formRepository.findById(logEntity.getFormId())
                .ifPresent(form -> dto.setFormName(form.getFormName()));

        return dto;
    }

    /**
     * 根据用户ID或账号获取用户名
     */
    private String getUserName(String userIdOrAccount) {
        if (userIdOrAccount == null || userIdOrAccount.isEmpty()) {
            return "系统";
        }
        try {
            // 尝试按ID或账号查询
            String sql = "SELECT name FROM bas_user WHERE id = ? OR account = ?";
            Query query = entityManager.createNativeQuery(sql);
            query.setParameter(1, userIdOrAccount);
            query.setParameter(2, userIdOrAccount);
            @SuppressWarnings("unchecked")
            List<String> results = query.getResultList();
            if (!results.isEmpty()) {
                return results.get(0);
            }
        } catch (Exception e) {
            log.debug("获取用户名失败: {}", e.getMessage());
        }
        return userIdOrAccount;
    }
}

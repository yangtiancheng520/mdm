package com.vueadmin.service.form;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vueadmin.dto.FormLogDto;
import com.vueadmin.entity.form.Form;
import com.vueadmin.entity.form.FormLog;
import com.vueadmin.repository.FormLogRepository;
import com.vueadmin.repository.FormRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 表单操作日志服务
 * 日志记录使用独立事务（REQUIRES_NEW），避免日志失败影响主业务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FormLogService {

    private final FormLogRepository formLogRepository;
    private final FormRepository formRepository;
    private final ObjectMapper objectMapper;

    /**
     * 记录表单操作日志（独立事务）
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logOperation(Long formId, String operationType, String operationDetail,
                            Integer fromVersion, Integer toVersion,
                            String fromStatus, String toStatus,
                            String changeData, String user) {
        try {
            FormLog logEntity = new FormLog();
            logEntity.setFormId(formId);
            logEntity.setOperationType(operationType);
            logEntity.setOperationDetail(operationDetail);
            logEntity.setFromVersion(fromVersion);
            logEntity.setToVersion(toVersion);
            logEntity.setFromStatus(fromStatus);
            logEntity.setToStatus(toStatus);
            logEntity.setChangeData(changeData);
            logEntity.setCreatedBy(user);

            formLogRepository.save(logEntity);
            log.info("记录表单操作日志: formId={}, operation={}, version: {} -> {}, status: {} -> {}",
                    formId, operationType, fromVersion, toVersion, fromStatus, toStatus);
        } catch (Exception e) {
            log.error("记录表单操作日志失败", e);
        }
    }

    /**
     * 记录创建日志
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logCreate(Form form, String user) {
        Map<String, Object> changeData = new HashMap<>();
        changeData.put("formCode", form.getFormCode());
        changeData.put("formName", form.getFormName());
        changeData.put("formType", form.getFormType());
        changeData.put("viewId", form.getViewId());

        String changeDataJson;
        try {
            changeDataJson = objectMapper.writeValueAsString(changeData);
        } catch (JsonProcessingException e) {
            changeDataJson = "{}";
        }

        logOperation(form.getId(), "create", "创建表单",
                null, 1,
                null, form.getStatus(),
                changeDataJson, user);
    }

    /**
     * 记录更新日志
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logUpdate(Form oldForm, Form newForm, String user) {
        Map<String, Object> changes = new HashMap<>();

        if (!oldForm.getFormName().equals(newForm.getFormName())) {
            changes.put("formName", Map.of("old", oldForm.getFormName(), "new", newForm.getFormName()));
        }
        if (!oldForm.getFormType().equals(newForm.getFormType())) {
            changes.put("formType", Map.of("old", oldForm.getFormType(), "new", newForm.getFormType()));
        }
        if (!java.util.Objects.equals(oldForm.getViewId(), newForm.getViewId())) {
            changes.put("viewId", Map.of("old", oldForm.getViewId(), "new", newForm.getViewId()));
        }
        if (!java.util.Objects.equals(oldForm.getDescription(), newForm.getDescription())) {
            changes.put("description", Map.of("old", oldForm.getDescription(), "new", newForm.getDescription()));
        }

        if (changes.isEmpty()) {
            return; // 没有变更，不记录日志
        }

        String changeDataJson;
        try {
            changeDataJson = objectMapper.writeValueAsString(changes);
        } catch (JsonProcessingException e) {
            changeDataJson = "{}";
        }

        logOperation(newForm.getId(), "update", "更新表单基本信息",
                newForm.getVersion(), newForm.getVersion(),
                null, null,
                changeDataJson, user);
    }

    /**
     * 记录保存设计日志
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logSaveDesign(Long formId, String user) {
        logOperation(formId, "save_design", "保存表单设计",
                null, null,
                null, null,
                null, user);
    }

    /**
     * 记录发布日志
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logPublish(Form form, int oldVersion, String oldStatus, int newVersion, String user) {
        logOperation(form.getId(), "publish", "发布表单",
                oldVersion, newVersion,
                oldStatus, "published",
                null, user);
    }

    /**
     * 记录取消发布日志
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logUnpublish(Form form, String oldStatus, String user) {
        logOperation(form.getId(), "unpublish", "取消发布表单",
                form.getVersion(), form.getVersion(),
                oldStatus, "draft",
                null, user);
    }

    /**
     * 记录删除日志
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logDelete(Long formId, String formCode, String formName, Integer version, String status, String user) {
        Map<String, Object> changeData = new HashMap<>();
        changeData.put("formCode", formCode);
        changeData.put("formName", formName);

        String changeDataJson;
        try {
            changeDataJson = objectMapper.writeValueAsString(changeData);
        } catch (JsonProcessingException e) {
            changeDataJson = "{}";
        }

        logOperation(formId, "delete", "删除表单",
                version, null,
                status, null,
                changeDataJson, user);
    }

    /**
     * 获取表单操作日志列表
     */
    public List<FormLogDto> getFormLogs(Long formId) {
        List<FormLog> logs = formLogRepository.findByFormIdOrderByCreatedAtDesc(formId);
        return logs.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 转换为DTO
     */
    private FormLogDto convertToDto(FormLog log) {
        FormLogDto dto = new FormLogDto();
        dto.setId(log.getId());
        dto.setFormId(log.getFormId());
        dto.setOperationType(log.getOperationType());
        dto.setOperationDetail(log.getOperationDetail());
        dto.setFromVersion(log.getFromVersion());
        dto.setToVersion(log.getToVersion());
        dto.setFromStatus(log.getFromStatus());
        dto.setToStatus(log.getToStatus());
        dto.setChangeData(log.getChangeData());
        dto.setCreatedBy(log.getCreatedBy());
        dto.setIpAddress(log.getIpAddress());

        // 格式化时间
        if (log.getCreatedAt() != null) {
            dto.setCreatedAt(log.getCreatedAt().toString());
        }

        // 生成显示文本
        dto.setVersionDisplay(formatVersionChange(log.getFromVersion(), log.getToVersion()));
        dto.setStatusDisplay(formatStatusChange(log.getFromStatus(), log.getToStatus()));

        return dto;
    }

    /**
     * 格式化版本变更显示
     */
    private String formatVersionChange(Integer fromVersion, Integer toVersion) {
        if (fromVersion == null && toVersion == null) {
            return null;
        }
        if (fromVersion == null) {
            return "V" + toVersion;
        }
        if (toVersion == null) {
            return "V" + fromVersion + " → 已删除";
        }
        if (fromVersion.equals(toVersion)) {
            return "V" + toVersion;
        }
        return "V" + fromVersion + " → V" + toVersion;
    }

    /**
     * 格式化状态变更显示
     */
    private String formatStatusChange(String fromStatus, String toStatus) {
        if (fromStatus == null && toStatus == null) {
            return null;
        }

        String fromLabel = getStatusLabel(fromStatus);
        String toLabel = getStatusLabel(toStatus);

        if (fromStatus == null) {
            return toLabel;
        }
        if (toStatus == null) {
            return fromLabel + " → 已删除";
        }
        if (fromStatus.equals(toStatus)) {
            return toLabel;
        }
        return fromLabel + " → " + toLabel;
    }

    /**
     * 获取状态标签
     */
    private String getStatusLabel(String status) {
        if (status == null) {
            return "-";
        }
        return switch (status) {
            case "draft" -> "草稿";
            case "published" -> "已发布";
            default -> status;
        };
    }
}

package com.vueadmin.service.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vueadmin.entity.data.DataOperationLog;
import com.vueadmin.entity.form.Form;
import com.vueadmin.repository.DataOperationLogRepository;
import com.vueadmin.repository.FormRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 操作日志记录服务
 * 负责记录主数据全生命周期的操作日志
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperationLogService {

    private final DataOperationLogRepository logRepository;
    private final FormRepository formRepository;
    private final ObjectMapper objectMapper;

    /**
     * 记录状态变更日志（增强版）
     */
    @Transactional
    public void logStatusChange(Long categoryId, Long formId, Long recordId,
                                String operationType, String operationDetail,
                                String fromStatus, String toStatus,
                                String reason, String user) {
        try {
            // 构建操作数据
            Map<String, Object> operationData = new HashMap<>();
            operationData.put("operation", operationType);
            operationData.put("operationDetail", operationDetail);
            operationData.put("fromStatus", fromStatus);
            operationData.put("toStatus", toStatus);
            if (reason != null && !reason.isEmpty()) {
                operationData.put("reason", reason);
            }
            operationData.put("timestamp", LocalDateTime.now().toString());
            operationData.put("user", user);

            DataOperationLog logEntity = new DataOperationLog();
            logEntity.setCategoryId(categoryId);
            logEntity.setFormId(formId);

            Form form = formRepository.findById(formId).orElse(null);
            logEntity.setViewId(form != null ? form.getViewId() : null);

            logEntity.setOperationType(operationType);
            logEntity.setOperationDetail(operationDetail);
            logEntity.setFromStatus(fromStatus);
            logEntity.setToStatus(toStatus);
            logEntity.setMainRecordId(recordId);
            logEntity.setOperationReason(reason);
            logEntity.setOperationData(objectMapper.writeValueAsString(operationData));
            logEntity.setStatus("active");
            logEntity.setCreatedBy(user);
            logEntity.setCreatedAt(LocalDateTime.now());

            logRepository.save(logEntity);
            log.info("记录状态变更日志: {} -> {}, 操作: {}, 用户: {}", fromStatus, toStatus, operationType, user);
        } catch (Exception e) {
            log.error("记录状态变更日志失败", e);
        }
    }

    /**
     * 记录质检日志（增强版）
     */
    @Transactional
    public void logQualityCheck(Long categoryId, Long formId, Long recordId,
                                String operationType, String operationDetail,
                                String fromStatus, String toStatus,
                                BigDecimal qualityScore, Object qualityIssues,
                                String user) {
        try {
            // 构建质检数据
            Map<String, Object> operationData = new HashMap<>();
            operationData.put("operation", operationType);
            operationData.put("operationDetail", operationDetail);
            operationData.put("fromStatus", fromStatus);
            operationData.put("toStatus", toStatus);
            operationData.put("qualityScore", qualityScore);
            operationData.put("timestamp", LocalDateTime.now().toString());
            operationData.put("user", user);

            if (qualityIssues != null) {
                operationData.put("qualityIssues", qualityIssues);
            }

            DataOperationLog logEntity = new DataOperationLog();
            logEntity.setCategoryId(categoryId);
            logEntity.setFormId(formId);

            Form form = formRepository.findById(formId).orElse(null);
            logEntity.setViewId(form != null ? form.getViewId() : null);

            logEntity.setOperationType(operationType);
            logEntity.setOperationDetail(operationDetail);
            logEntity.setFromStatus(fromStatus);
            logEntity.setToStatus(toStatus);
            logEntity.setMainRecordId(recordId);
            logEntity.setQualityScore(qualityScore);
            logEntity.setOperationData(objectMapper.writeValueAsString(operationData));

            if (qualityIssues != null) {
                logEntity.setQualityIssues(objectMapper.writeValueAsString(qualityIssues));
            }

            logEntity.setStatus("active");
            logEntity.setCreatedBy(user);
            logEntity.setCreatedAt(LocalDateTime.now());

            logRepository.save(logEntity);
            log.info("记录质检日志: 评分={}, 操作={}, 用户={}", qualityScore, operationType, user);
        } catch (JsonProcessingException e) {
            log.error("记录质检日志失败", e);
        }
    }

    /**
     * 记录提交审批日志（增强版）
     */
    @Transactional
    public void logSubmitApproval(Long categoryId, Long formId, Long recordId, String user) {
        logStatusChange(categoryId, formId, recordId,
                "submit", "提交审批",
                "DRAFT", "PENDING",
                null, user);
    }

    /**
     * 记录通过审批日志（增强版）
     */
    @Transactional
    public void logApprove(Long categoryId, Long formId, Long recordId, String user) {
        logStatusChange(categoryId, formId, recordId,
                "approve", "通过审批",
                "PENDING", "PENDING_QC",
                null, user);
    }

    /**
     * 记录驳回审批日志（增强版）
     */
    @Transactional
    public void logReject(Long categoryId, Long formId, Long recordId, String reason, String user) {
        logStatusChange(categoryId, formId, recordId,
                "reject", "驳回审批",
                "PENDING", "DRAFT",
                reason, user);
    }

    /**
     * 记录质检合格日志（增强版）
     */
    @Transactional
    public void logQualityPass(Long categoryId, Long formId, Long recordId,
                               BigDecimal qualityScore, Object checkResults, String user) {
        logQualityCheck(categoryId, formId, recordId,
                "qc_pass", "质检合格",
                "PENDING_QC", "ACTIVE_QUALIFIED",
                qualityScore, checkResults, user);
    }

    /**
     * 记录质检不合格日志（增强版）
     */
    @Transactional
    public void logQualityFail(Long categoryId, Long formId, Long recordId,
                               BigDecimal qualityScore, Object qualityIssues, String user) {
        logQualityCheck(categoryId, formId, recordId,
                "qc_fail", "质检不合格",
                "PENDING_QC", "ACTIVE_UNQUALIFIED",
                qualityScore, qualityIssues, user);
    }

    /**
     * 记录作废日志（增强版）
     */
    @Transactional
    public void logObsolete(Long categoryId, Long formId, Long recordId, String reason, String user) {
        logStatusChange(categoryId, formId, recordId,
                "obsolete", "作废数据",
                "ACTIVE_QUALIFIED", "OBSOLETE",
                reason, user);
    }

    /**
     * 记录恢复日志（增强版）
     */
    @Transactional
    public void logRestore(Long categoryId, Long formId, Long recordId, String user) {
        logStatusChange(categoryId, formId, recordId,
                "restore", "恢复数据",
                "OBSOLETE", "DRAFT",
                null, user);
    }

    /**
     * 记录分发日志（增强版）
     */
    @Transactional
    public void logDistribute(Long categoryId, Long formId, Long recordId,
                              String targetSystem, String result, String user) {
        try {
            Map<String, Object> operationData = new HashMap<>();
            operationData.put("operation", "distribute");
            operationData.put("targetSystem", targetSystem);
            operationData.put("result", result);
            operationData.put("timestamp", LocalDateTime.now().toString());
            operationData.put("user", user);

            DataOperationLog logEntity = new DataOperationLog();
            logEntity.setCategoryId(categoryId);
            logEntity.setFormId(formId);

            Form form = formRepository.findById(formId).orElse(null);
            logEntity.setViewId(form != null ? form.getViewId() : null);

            logEntity.setOperationType("distribute");
            logEntity.setOperationDetail("分发到下游系统: " + targetSystem);
            logEntity.setMainRecordId(recordId);
            logEntity.setOperationData(objectMapper.writeValueAsString(operationData));
            logEntity.setOperationReason("分发结果: " + result);
            logEntity.setStatus("active");
            logEntity.setCreatedBy(user);
            logEntity.setCreatedAt(LocalDateTime.now());

            logRepository.save(logEntity);
            log.info("记录分发日志: targetSystem={}, result={}, user={}", targetSystem, result, user);
        } catch (JsonProcessingException e) {
            log.error("记录分发日志失败", e);
        }
    }

    /**
     * 记录创建日志
     */
    @Transactional
    public void logCreate(Long categoryId, Long formId, Long recordId, String user) {
        logStatusChange(categoryId, formId, recordId,
                "create", "创建数据",
                null, "DRAFT",
                null, user);
    }

    /**
     * 记录更新日志
     */
    @Transactional
    public void logUpdate(Long categoryId, Long formId, Long recordId, String user) {
        logStatusChange(categoryId, formId, recordId,
                "update", "更新数据",
                null, null,
                null, user);
    }

    /**
     * 记录删除日志
     */
    @Transactional
    public void logDelete(Long categoryId, Long formId, Long recordId, String user) {
        logStatusChange(categoryId, formId, recordId,
                "delete", "删除数据",
                null, null,
                null, user);
    }
}

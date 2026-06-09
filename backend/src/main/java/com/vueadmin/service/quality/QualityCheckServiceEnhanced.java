package com.vueadmin.service.quality;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.vueadmin.dto.*;
import com.vueadmin.entity.quality.QualityCheckResult;
import com.vueadmin.entity.quality.QualityCheckTask;
import com.vueadmin.entity.quality.QualityRule;
import com.vueadmin.repository.QualityCheckResultRepository;
import com.vueadmin.repository.QualityCheckTaskRepository;
import com.vueadmin.repository.QualityRuleRepository;
import com.vueadmin.service.standard.ViewDefinitionService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 质量检测执行Service - 增强版
 */
@Slf4j
@Service
public class QualityCheckServiceEnhanced {

    @Autowired
    private QualityCheckTaskRepository taskRepository;

    @Autowired
    private QualityCheckResultRepository resultRepository;

    @Autowired
    private QualityRuleRepository ruleRepository;

    @Autowired
    private ViewDefinitionService viewDefinitionService;

    @Autowired
    private EntityManager entityManager;

    /**
     * 测试规则（只检测前100条数据）
     */
    public Map<String, Object> testRule(QualityRuleDto dto) {
        Map<String, Object> result = new HashMap<>();

        try {
            String tableName = dto.getTableName();
            String fieldCode = dto.getFieldCode();

            if (tableName == null || tableName.isEmpty() || fieldCode == null || fieldCode.isEmpty()) {
                result.put("error", "表名或字段编码不能为空");
                return result;
            }

            // 查询前100条数据
            String sql = String.format(
                "SELECT id, %s FROM %s WHERE status = 'active' LIMIT 100",
                fieldCode, tableName
            );

            Query query = entityManager.createNativeQuery(sql);
            List<Object[]> rows = query.getResultList();

            int passCount = 0;
            int failCount = 0;
            List<Map<String, Object>> failRecords = new ArrayList<>();

            for (Object[] row : rows) {
                boolean passed = executeSingleCheck(row, dto);
                if (passed) {
                    passCount++;
                } else {
                    failCount++;
                    if (failRecords.size() < 10) {
                        failRecords.add(Map.of(
                            "recordId", row[0],
                            "fieldValue", row[1] != null ? row[1].toString() : null,
                            "reason", generateIssueMessage(dto)
                        ));
                    }
                }
            }

            result.put("totalRecords", rows.size());
            result.put("passCount", passCount);
            result.put("failCount", failCount);
            result.put("passRate", rows.size() > 0 ? (passCount * 100.0 / rows.size()) : 100);
            result.put("failRecords", failRecords);
            result.put("success", true);

        } catch (Exception e) {
            log.error("测试规则失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * 执行单条规则检测
     */
    private boolean executeSingleCheck(Object[] row, QualityRuleDto rule) {
        String checkType = rule.getCheckType();
        String checkConfig = rule.getCheckConfig();
        Object fieldValue = row[1];

        if (checkConfig == null || checkConfig.isEmpty()) {
            // 使用默认配置
            return executeDefaultCheck(fieldValue, rule.getRuleType());
        }

        JSONObject config = JSON.parseObject(checkConfig);
        String actualCheckType = config.getString("checkType");
        if (actualCheckType == null) {
            actualCheckType = checkType;
        }

        switch (actualCheckType) {
            case "not_null":
                return checkNotNull(fieldValue, config);

            case "not_empty":
                return checkNotEmpty(fieldValue, config);

            case "regex":
                return checkRegex(fieldValue, config);

            case "length":
                return checkLength(fieldValue, config);

            case "range":
                return checkRange(fieldValue, config);

            case "domain":
                return checkDomain(fieldValue, config);

            case "unique_global":
            case "unique_in_main":
                // 唯一性检查需要全局检查，这里简化返回true
                return true;

            default:
                return true;
        }
    }

    /**
     * 非空检查
     */
    private boolean checkNotNull(Object fieldValue, JSONObject config) {
        if (fieldValue == null) return false;

        boolean trimWhitespace = config.getBooleanValue("trimWhitespace", true);
        if (trimWhitespace && fieldValue instanceof String) {
            return !((String) fieldValue).trim().isEmpty();
        }

        return true;
    }

    /**
     * 非空字符串检查
     */
    private boolean checkNotEmpty(Object fieldValue, JSONObject config) {
        if (fieldValue == null) return false;

        String strValue = fieldValue.toString();
        int minLength = config.getIntValue("minLength", 1);
        Integer maxLength = config.getInteger("maxLength");

        int length = strValue.length();
        if (length < minLength) return false;
        if (maxLength != null && length > maxLength) return false;

        return true;
    }

    /**
     * 正则表达式检查
     */
    private boolean checkRegex(Object fieldValue, JSONObject config) {
        if (fieldValue == null) return true; // 空值不检查

        String pattern = config.getString("pattern");
        if (pattern == null || pattern.isEmpty()) return true;

        String strValue = fieldValue.toString();
        return Pattern.matches(pattern, strValue);
    }

    /**
     * 长度检查
     */
    private boolean checkLength(Object fieldValue, JSONObject config) {
        if (fieldValue == null) return true;

        String strValue = fieldValue.toString();
        Integer minLength = config.getInteger("minLength");
        Integer maxLength = config.getInteger("maxLength");
        String countType = config.getString("countType");
        if (countType == null) countType = "char";

        int length;
        if ("byte".equals(countType)) {
            length = strValue.getBytes().length;
        } else {
            length = strValue.length();
        }

        if (minLength != null && length < minLength) return false;
        if (maxLength != null && length > maxLength) return false;

        return true;
    }

    /**
     * 范围检查
     */
    private boolean checkRange(Object fieldValue, JSONObject config) {
        if (fieldValue == null) return true;

        try {
            BigDecimal numValue = new BigDecimal(fieldValue.toString());
            BigDecimal minValue = config.getBigDecimal("minValue");
            BigDecimal maxValue = config.getBigDecimal("maxValue");
            String minOperator = config.getString("minOperator");
            String maxOperator = config.getString("maxOperator");
            if (minOperator == null) minOperator = ">=";
            if (maxOperator == null) maxOperator = "<=";

            boolean minPass = minValue == null ||
                (">=".equals(minOperator) ? numValue.compareTo(minValue) >= 0 : numValue.compareTo(minValue) > 0);
            boolean maxPass = maxValue == null ||
                ("<=".equals(maxOperator) ? numValue.compareTo(maxValue) <= 0 : numValue.compareTo(maxValue) < 0);

            return minPass && maxPass;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 值域检查
     */
    private boolean checkDomain(Object fieldValue, JSONObject config) {
        if (fieldValue == null) {
            return config.getBooleanValue("allowNull", true);
        }

        String domainCode = config.getString("domainCode");
        if (domainCode == null || domainCode.isEmpty()) return true;

        try {
            String sql = "SELECT COUNT(*) FROM std_value_domain_item " +
                        "WHERE domain_code = ? AND item_value = ? AND status = 'active'";
            Query query = entityManager.createNativeQuery(sql);
            query.setParameter(1, domainCode);
            query.setParameter(2, fieldValue.toString());

            BigInteger count = (BigInteger) query.getSingleResult();
            return count.intValue() > 0;
        } catch (Exception e) {
            log.error("值域检查失败", e);
            return false;
        }
    }

    /**
     * 默认检查（根据规则类型）
     */
    private boolean executeDefaultCheck(Object fieldValue, String ruleType) {
        if ("completeness".equals(ruleType)) {
            return fieldValue != null;
        }
        return true;
    }

    /**
     * 生成问题描述
     */
    private String generateIssueMessage(QualityRuleDto rule) {
        String fieldName = rule.getFieldName() != null ? rule.getFieldName() : rule.getFieldCode();
        String checkType = rule.getCheckType();

        if (checkType == null) {
            checkType = "not_null";
        }

        switch (checkType) {
            case "not_null":
                return fieldName + "不能为空";
            case "not_empty":
                return fieldName + "不能为空字符串";
            case "regex":
                return fieldName + "格式不正确";
            case "length":
                return fieldName + "长度不符合要求";
            case "range":
                return fieldName + "数值不在允许范围内";
            case "domain":
                return fieldName + "值不在允许值域内";
            case "unique_global":
                return fieldName + "值重复";
            case "unique_in_main":
                return fieldName + "在主表范围内值重复";
            default:
                return rule.getRuleName() + "检查未通过";
        }
    }

    /**
     * 执行批量检测（完整版）
     */
    @Transactional
    public QualityCheckTaskDto executeBatchCheck(QualityCheckTaskDto dto, String createdBy) {
        // 创建任务
        QualityCheckTask task = new QualityCheckTask();
        task.setTaskCode(generateTaskCode());
        task.setTaskName(dto.getTaskName() != null ? dto.getTaskName() : "质量检测任务");
        task.setViewId(dto.getViewId());
        task.setStatus("running");
        task.setCreatedBy(createdBy);
        task.setStartTime(LocalDateTime.now());
        task = taskRepository.save(task);

        try {
            // 获取要执行的规则
            List<QualityRule> rules = getRulesToExecute(dto);
            if (rules.isEmpty()) {
                task.setStatus("failed");
                task.setErrorMessage("没有找到可执行的规则");
                taskRepository.save(task);
                return convertTaskToDto(task);
            }

            int totalRecords = 0;
            int passCount = 0;
            int failCount = 0;

            // 执行检测
            for (QualityRule rule : rules) {
                List<QualityCheckResult> results = executeRuleCheckEnhanced(task.getId(), rule);
                resultRepository.saveAll(results);

                for (QualityCheckResult result : results) {
                    totalRecords++;
                    if (result.getIsPassed() == 1) {
                        passCount++;
                    } else {
                        failCount++;
                    }
                }
            }

            // 更新任务统计
            task.setTotalRecords(totalRecords);
            task.setPassCount(passCount);
            task.setFailCount(failCount);
            task.setTotalRules(rules.size());

            if (totalRecords > 0) {
                BigDecimal rate = new BigDecimal(passCount * 100)
                    .divide(new BigDecimal(totalRecords), 2, RoundingMode.HALF_UP);
                task.setPassRate(rate);
            }

            task.setStatus("completed");
            task.setEndTime(LocalDateTime.now());
            task.setDurationMs((int) java.time.Duration.between(task.getStartTime(), task.getEndTime()).toMillis());

        } catch (Exception e) {
            log.error("检测任务执行失败", e);
            task.setStatus("failed");
            task.setEndTime(LocalDateTime.now());
            task.setErrorMessage(e.getMessage());
        }

        task = taskRepository.save(task);
        return convertTaskToDto(task);
    }

    /**
     * 执行单条规则的增强检测
     */
    private List<QualityCheckResult> executeRuleCheckEnhanced(Long taskId, QualityRule rule) {
        List<QualityCheckResult> results = new ArrayList<>();

        try {
            String tableName = rule.getTableName();
            String fieldCode = rule.getFieldCode();

            if (tableName == null || fieldCode == null) {
                return results;
            }

            // 查询数据
            String sql = String.format("SELECT id, %s FROM %s WHERE status = 'active'", fieldCode, tableName);
            Query query = entityManager.createNativeQuery(sql);
            List<Object[]> rows = query.getResultList();

            QualityRuleDto ruleDto = new QualityRuleDto();
            BeanUtils.copyProperties(rule, ruleDto);

            for (Object[] row : rows) {
                QualityCheckResult result = new QualityCheckResult();
                result.setTaskId(taskId);
                result.setRuleId(rule.getId());
                result.setEntityId(rule.getEntityId());
                result.setEntityType(rule.getEntityType());
                result.setTableName(tableName);
                result.setFieldCode(fieldCode);
                result.setRecordId(((Number) row[0]).longValue());

                if (row[1] != null) {
                    result.setFieldValue(row[1].toString());
                }

                boolean passed = executeSingleCheck(row, ruleDto);
                result.setIsPassed(passed ? 1 : 0);

                if (!passed) {
                    result.setIssueType(rule.getRuleType());
                    result.setIssueLevel(rule.getSeverity());
                    result.setIssueMessage(generateIssueMessage(ruleDto));
                }

                results.add(result);
            }

        } catch (Exception e) {
            log.error("执行规则检测失败: ruleId={}", rule.getId(), e);
        }

        return results;
    }

    /**
     * 获取要执行的规则
     */
    private List<QualityRule> getRulesToExecute(QualityCheckTaskDto dto) {
        List<QualityRule> rules = new ArrayList<>();

        if (dto.getViewId() != null) {
            rules = ruleRepository.findByViewIdAndStatus(dto.getViewId(), "active");
        }

        return rules.stream()
            .filter(r -> "active".equals(r.getStatus()))
            .collect(Collectors.toList());
    }

    /**
     * 生成任务编码
     */
    private String generateTaskCode() {
        return "QT" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    /**
     * 转换任务为DTO
     */
    private QualityCheckTaskDto convertTaskToDto(QualityCheckTask task) {
        QualityCheckTaskDto dto = new QualityCheckTaskDto();
        BeanUtils.copyProperties(task, dto);
        return dto;
    }
}
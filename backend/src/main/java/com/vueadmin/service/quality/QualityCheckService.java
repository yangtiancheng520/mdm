package com.vueadmin.service.quality;

import com.vueadmin.dto.*;
import com.vueadmin.entity.quality.QualityCheckResult;
import com.vueadmin.entity.quality.QualityCheckTask;
import com.vueadmin.entity.quality.QualityIssue;
import com.vueadmin.entity.quality.QualityRule;
import com.vueadmin.exception.BusinessException;
import com.vueadmin.repository.QualityCheckResultRepository;
import com.vueadmin.repository.QualityCheckTaskRepository;
import com.vueadmin.repository.QualityIssueRepository;
import com.vueadmin.repository.QualityRuleRepository;
import com.vueadmin.service.standard.ViewDefinitionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 质量检测执行Service
 */
@Slf4j
@Service
public class QualityCheckService {

    @Autowired
    private QualityCheckTaskRepository taskRepository;

    @Autowired
    private QualityCheckResultRepository resultRepository;

    @Autowired
    private QualityRuleRepository ruleRepository;

    @Autowired
    private QualityIssueRepository issueRepository;

    @Autowired
    private ViewDefinitionService viewDefinitionService;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 获取检测任务列表
     */
    public List<QualityCheckTaskDto> getTaskList(Long viewId, String status) {
        List<QualityCheckTask> tasks;

        if (viewId != null) {
            tasks = taskRepository.findByViewId(viewId);
        } else if (status != null && !status.isEmpty()) {
            tasks = taskRepository.findByStatus(status);
        } else {
            tasks = taskRepository.findAll();
        }

        return tasks.stream()
                .map(this::convertTaskToDto)
                .collect(Collectors.toList());
    }

    /**
     * 获取任务详情
     */
    public QualityCheckTaskDto getTaskById(Long id) {
        QualityCheckTask task = taskRepository.findById(id)
                .orElseThrow(() -> new BusinessException("任务不存在"));
        return convertTaskToDto(task);
    }

    /**
     * 获取检测结果列表
     */
    public List<QualityCheckResultDto> getResultsByTaskId(Long taskId) {
        List<QualityCheckResult> results = resultRepository.findByTaskId(taskId);
        return results.stream()
                .map(this::convertResultToDto)
                .collect(Collectors.toList());
    }

    /**
     * 获取检测失败的结果
     */
    public List<QualityCheckResultDto> getFailedResults(Long taskId) {
        List<QualityCheckResult> results = resultRepository.findByTaskIdAndIsPassed(taskId, 0);
        return results.stream()
                .map(this::convertResultToDto)
                .collect(Collectors.toList());
    }

    /**
     * 创建并执行检测任务
     */
    @Transactional
    public QualityCheckTaskDto executeCheck(QualityCheckTaskDto dto, String createdBy) {
        // 生成任务编码
        String taskCode = generateTaskCode();
        String taskName = dto.getTaskName() != null ? dto.getTaskName() : "质量检测任务-" + taskCode;

        // 创建任务记录
        QualityCheckTask task = new QualityCheckTask();
        task.setTaskCode(taskCode);
        task.setTaskName(taskName);
        task.setViewId(dto.getViewId());
        task.setEntityIds(dto.getEntityIds());
        task.setRuleIds(dto.getRuleIds());
        task.setStatus("running");
        task.setCreatedBy(createdBy);
        task.setStartTime(LocalDateTime.now());

        task = taskRepository.save(task);

        try {
            // 判断是否指定了记录列表
            boolean hasSpecificRecords = dto.getRecords() != null && !dto.getRecords().isEmpty();
            log.info("执行检测任务: taskCode={}, hasSpecificRecords={}, recordsCount={}",
                taskCode, hasSpecificRecords, dto.getRecords() != null ? dto.getRecords().size() : 0);

            // 获取要执行的规则
            List<QualityRule> rules;
            if (hasSpecificRecords) {
                // 根据记录列表获取所有涉及的视图ID
                Set<Long> viewIds = dto.getRecords().stream()
                    .map(QualityCheckTaskDto.CheckRecordDto::getViewId)
                    .collect(Collectors.toSet());
                log.info("涉及的视图ID: {}", viewIds);
                rules = ruleRepository.findByViewIdInAndStatus(new ArrayList<>(viewIds), "active");
                log.info("找到的规则数量: {}", rules.size());
            } else {
                rules = getRulesToExecute(dto);
            }

            if (rules.isEmpty()) {
                log.warn("没有找到可执行的规则，跳过检测");
                // 不抛出异常，直接返回空结果
                task.setStatus("completed");
                task.setEndTime(LocalDateTime.now());
                task.setTotalRecords(0);
                task.setPassCount(0);
                task.setFailCount(0);
                task = taskRepository.save(task);
                return convertTaskToDto(task);
            }

            task.setTotalRules(rules.size());
            task.setRuleIds(rules.stream().map(r -> r.getId().toString()).collect(Collectors.joining(",")));

            // 执行检测
            int totalRecords = 0;
            int passCount = 0;
            int failCount = 0;
            int mainTotal = 0, mainPass = 0, mainFail = 0;
            int subTotal = 0, subPass = 0, subFail = 0;

            for (QualityRule rule : rules) {
                List<QualityCheckResult> results;

                if (hasSpecificRecords) {
                    // 针对指定记录检测
                    results = executeRuleCheckForRecords(task.getId(), rule, dto.getRecords());
                } else {
                    // 原有逻辑：检测所有数据
                    results = executeRuleCheck(task.getId(), rule);
                }

                for (QualityCheckResult result : results) {
                    totalRecords++;
                    if (result.getIsPassed() == 1) {
                        passCount++;
                        if ("main".equals(result.getEntityType())) {
                            mainTotal++;
                            mainPass++;
                        } else {
                            subTotal++;
                            subPass++;
                        }
                    } else {
                        failCount++;
                        if ("main".equals(result.getEntityType())) {
                            mainTotal++;
                            mainFail++;
                        } else {
                            subTotal++;
                            subFail++;
                        }
                    }
                }

                // 保存结果
                resultRepository.saveAll(results);

                // 生成问题记录
                createIssuesFromResults(results, task.getTaskCode());
            }

            // 更新任务统计
            task.setTotalRecords(totalRecords);
            task.setPassCount(passCount);
            task.setFailCount(failCount);
            task.setMainTotal(mainTotal);
            task.setMainPass(mainPass);
            task.setMainFail(mainFail);
            task.setSubTotal(subTotal);
            task.setSubPass(subPass);
            task.setSubFail(subFail);

            if (totalRecords > 0) {
                BigDecimal rate = new BigDecimal(passCount)
                        .multiply(new BigDecimal(100))
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
     * 获取要执行的规则
     */
    private List<QualityRule> getRulesToExecute(QualityCheckTaskDto dto) {
        List<QualityRule> rules = new ArrayList<>();

        // 如果指定了规则ID
        if (dto.getRuleIds() != null && !dto.getRuleIds().isEmpty()) {
            List<Long> ruleIdList = Arrays.stream(dto.getRuleIds().split(","))
                    .map(String::trim)
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
            rules = ruleRepository.findAllById(ruleIdList);
        }
        // 如果指定了实体ID
        else if (dto.getEntityIds() != null && !dto.getEntityIds().isEmpty()) {
            List<Long> entityIdList = Arrays.stream(dto.getEntityIds().split(","))
                    .map(String::trim)
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
            rules = ruleRepository.findByEntityIds(entityIdList);
        }
        // 如果指定了视图ID
        else if (dto.getViewId() != null) {
            rules = ruleRepository.findByViewIdAndStatus(dto.getViewId(), "active");
        }

        // 只执行启用状态的规则
        return rules.stream()
                .filter(r -> "active".equals(r.getStatus()))
                .collect(Collectors.toList());
    }

    /**
     * 执行单条规则检测
     */
    private List<QualityCheckResult> executeRuleCheck(Long taskId, QualityRule rule) {
        List<QualityCheckResult> results = new ArrayList<>();

        String tableName = rule.getTableName();
        String fieldCode = rule.getFieldCode();
        String ruleType = rule.getRuleType();

        if (tableName == null || tableName.isEmpty()) {
            return results;
        }

        try {
            // 根据规则类型生成检测SQL
            String checkSql = generateCheckSql(rule);

            if (checkSql == null || checkSql.isEmpty()) {
                return results;
            }

            log.info("执行检测SQL: {}", checkSql);

            Query query = entityManager.createNativeQuery(checkSql);
            @SuppressWarnings("unchecked")
            List<Object[]> rows = query.getResultList();

            for (Object[] row : rows) {
                QualityCheckResult result = new QualityCheckResult();
                result.setTaskId(taskId);
                result.setRuleId(rule.getId());
                result.setEntityId(rule.getEntityId());
                result.setEntityType(rule.getEntityType());
                result.setTableName(tableName);
                result.setFieldCode(fieldCode);

                // 解析结果
                if (row.length >= 1) {
                    result.setRecordId(((Number) row[0]).longValue());
                }
                if (row.length >= 2 && row[1] != null) {
                    result.setFieldValue(row[1].toString());
                }

                // 根据规则类型判断是否通过
                boolean isPassed = isResultPassed(row, rule);
                result.setIsPassed(isPassed ? 1 : 0);

                if (!isPassed) {
                    result.setIssueType(ruleType);
                    result.setIssueLevel(rule.getSeverity());
                    result.setIssueMessage(generateIssueMessage(rule));
                }

                results.add(result);
            }

        } catch (Exception e) {
            log.error("执行规则检测失败: ruleId={}, error={}", rule.getId(), e.getMessage());
        }

        return results;
    }

    /**
     * 针对指定记录执行单条规则检测
     */
    private List<QualityCheckResult> executeRuleCheckForRecords(Long taskId, QualityRule rule,
            List<QualityCheckTaskDto.CheckRecordDto> records) {
        List<QualityCheckResult> results = new ArrayList<>();

        String tableName = rule.getTableName();
        String fieldCode = rule.getFieldCode();
        String ruleType = rule.getRuleType();

        log.info("执行规则检测: ruleId={}, ruleName={}, tableName={}, fieldCode={}",
            rule.getId(), rule.getRuleName(), tableName, fieldCode);
        log.info("传入的记录列表: {}", records.stream()
            .map(r -> "viewId=" + r.getViewId() + ",tableName=" + r.getTableName() + ",recordId=" + r.getRecordId())
            .collect(Collectors.toList()));

        if (tableName == null || tableName.isEmpty() || fieldCode == null || fieldCode.isEmpty()) {
            log.warn("规则缺少tableName或fieldCode，跳过");
            return results;
        }

        // 过滤出当前规则对应表的记录
        List<Long> recordIds = records.stream()
            .filter(r -> tableName.equals(r.getTableName()))
            .map(QualityCheckTaskDto.CheckRecordDto::getRecordId)
            .collect(Collectors.toList());

        log.info("过滤后的recordIds: {}", recordIds);

        if (recordIds.isEmpty()) {
            log.warn("没有匹配的记录，跳过检测");
            return results;
        }

        try {
            // 构建针对指定记录的检测SQL
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT id, ").append(fieldCode);

            // 如果是子表，需要关联主表ID
            if ("sub".equals(rule.getEntityType())) {
                sql.append(", main_id");
            }

            sql.append(" FROM ").append(tableName);
            sql.append(" WHERE id IN (").append(recordIds.stream().map(String::valueOf).collect(Collectors.joining(","))).append(")");

            log.info("执行指定记录检测SQL: {}", sql.toString());

            Query query = entityManager.createNativeQuery(sql.toString());
            @SuppressWarnings("unchecked")
            List<Object[]> rows = query.getResultList();

            for (Object[] row : rows) {
                QualityCheckResult result = new QualityCheckResult();
                result.setTaskId(taskId);
                result.setRuleId(rule.getId());
                result.setEntityId(rule.getEntityId());
                result.setEntityType(rule.getEntityType());
                result.setTableName(tableName);
                result.setFieldCode(fieldCode);

                // 解析结果
                if (row.length >= 1) {
                    result.setRecordId(((Number) row[0]).longValue());
                }
                if (row.length >= 2 && row[1] != null) {
                    result.setFieldValue(row[1].toString());
                }

                // 根据规则类型判断是否通过
                boolean isPassed = isResultPassed(row, rule);
                result.setIsPassed(isPassed ? 1 : 0);

                if (!isPassed) {
                    result.setIssueType(ruleType);
                    result.setIssueLevel(rule.getSeverity());
                    result.setIssueMessage(generateIssueMessage(rule));
                }

                results.add(result);
            }

        } catch (Exception e) {
            log.error("执行指定记录规则检测失败: ruleId={}, error={}", rule.getId(), e.getMessage());
        }

        return results;
    }

    /**
     * 生成检测SQL
     */
    private String generateCheckSql(QualityRule rule) {
        String tableName = rule.getTableName();
        String fieldCode = rule.getFieldCode();
        String ruleType = rule.getRuleType();

        if (fieldCode == null || fieldCode.isEmpty()) {
            return null;
        }

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT id, ").append(fieldCode);

        // 如果是子表，需要关联主表ID
        if ("sub".equals(rule.getEntityType())) {
            sql.append(", main_id");
        }

        sql.append(" FROM ").append(tableName);
        // 不限制状态，检测所有数据
        // sql.append(" WHERE status = 'active'");

        return sql.toString();
    }

    /**
     * 判断检测结果是否通过
     */
    private boolean isResultPassed(Object[] row, QualityRule rule) {
        String ruleType = rule.getRuleType();
        String fieldValue = row.length >= 2 && row[1] != null ? row[1].toString() : null;

        switch (ruleType) {
            case "completeness":
                // 完整性检查：非空
                return fieldValue != null && !fieldValue.trim().isEmpty();

            case "uniqueness":
                // 唯一性检查需要特殊处理，这里简化为通过
                return true;

            case "accuracy":
                // 准确性检查：格式、值域等
                return checkAccuracy(fieldValue, rule);

            default:
                return true;
        }
    }

    /**
     * 准确性检查
     */
    private boolean checkAccuracy(String fieldValue, QualityRule rule) {
        if (fieldValue == null || fieldValue.trim().isEmpty()) {
            // 检查是否允许空值
            String checkConfig = rule.getCheckConfig();
            if (checkConfig != null && checkConfig.contains("\"allowNull\":false")) {
                return false;
            }
            return true; // 空值默认通过
        }

        String checkType = rule.getCheckType();
        String checkConfig = rule.getCheckConfig();

        try {
            // 解析检查配置
            Map<String, Object> config = checkConfig != null ?
                objectMapper.readValue(checkConfig, Map.class) : new HashMap<>();

            switch (checkType) {
                case "regex":
                    // 正则表达式检查
                    String pattern = (String) config.get("pattern");
                    if (pattern != null) {
                        return fieldValue.matches(pattern);
                    }
                    break;

                case "domain":
                    // 值域检查
                    String domainCode = (String) config.get("domainCode");
                    if (domainCode != null) {
                        return checkDomainValue(fieldValue, domainCode);
                    }
                    break;

                case "length":
                    // 长度检查
                    Integer minLength = getInteger(config, "minLength");
                    Integer maxLength = getInteger(config, "maxLength");
                    int len = fieldValue.length();
                    if (minLength != null && len < minLength) {
                        return false;
                    }
                    if (maxLength != null && len > maxLength) {
                        return false;
                    }
                    return true;

                case "range":
                    // 范围检查
                    try {
                        double value = Double.parseDouble(fieldValue);
                        Double minValue = getDouble(config, "minValue");
                        Double maxValue = getDouble(config, "maxValue");
                        String minOperator = (String) config.getOrDefault("minOperator", ">=");
                        String maxOperator = (String) config.getOrDefault("maxOperator", "<=");

                        if (minValue != null) {
                            if (">=".equals(minOperator) && value < minValue) return false;
                            if (">".equals(minOperator) && value <= minValue) return false;
                        }
                        if (maxValue != null) {
                            if ("<=".equals(maxOperator) && value > maxValue) return false;
                            if ("<".equals(maxOperator) && value >= maxValue) return false;
                        }
                        return true;
                    } catch (NumberFormatException e) {
                        return false; // 不是数字
                    }

                default:
                    return true;
            }
        } catch (Exception e) {
            log.warn("解析检查配置失败: {}", checkConfig);
        }

        return true;
    }

    /**
     * 检查值域
     */
    private boolean checkDomainValue(String value, String domainCode) {
        try {
            String sql = "SELECT COUNT(*) FROM std_value_domain_item di " +
                        "JOIN std_value_domain d ON di.domain_id = d.id " +
                        "WHERE d.domain_code = ? AND (di.item_value = ? OR di.item_code = ?)";
            Query query = entityManager.createNativeQuery(sql);
            query.setParameter(1, domainCode);
            query.setParameter(2, value);
            query.setParameter(3, value);

            Number count = (Number) query.getSingleResult();
            return count.intValue() > 0;
        } catch (Exception e) {
            log.warn("值域检查失败: domainCode={}, value={}", domainCode, value);
            return true;
        }
    }

    /**
     * 从配置中获取整数
     */
    private Integer getInteger(Map<String, Object> config, String key) {
        Object value = config.get(key);
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).intValue();
        try {
            return Integer.parseInt(value.toString());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从配置中获取浮点数
     */
    private Double getDouble(Map<String, Object> config, String key) {
        Object value = config.get(key);
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).doubleValue();
        try {
            return Double.parseDouble(value.toString());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从检测结果生成问题记录
     */
    private void createIssuesFromResults(List<QualityCheckResult> results, String taskCode) {
        List<QualityIssue> issues = new ArrayList<>();

        for (QualityCheckResult result : results) {
            if (result.getIsPassed() == 0) { // 失败的记录
                // 获取规则信息
                QualityRule rule = ruleRepository.findById(result.getRuleId()).orElse(null);

                QualityIssue issue = new QualityIssue();
                issue.setIssueCode(generateIssueCode());
                issue.setIssueType(result.getIssueType());
                issue.setIssueLevel(result.getIssueLevel());
                issue.setIssueDesc(result.getIssueMessage());
                issue.setRuleId(result.getRuleId());
                issue.setEntityId(result.getEntityId());
                issue.setEntityType(result.getEntityType());
                issue.setRecordId(result.getRecordId());
                issue.setFieldCode(result.getFieldCode());
                issue.setFieldValue(result.getFieldValue());
                issue.setTableName(result.getTableName());

                // 从规则获取视图ID
                if (rule != null) {
                    issue.setViewId(rule.getViewId());
                }

                issue.setStatus("open");
                issues.add(issue);
            }
        }

        if (!issues.isEmpty()) {
            issueRepository.saveAll(issues);
            log.info("生成问题记录: {} 条", issues.size());
        }
    }

    /**
     * 生成问题编码
     */
    private String generateIssueCode() {
        return "ISS" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", new Random().nextInt(10000));
    }

    /**
     * 生成问题描述
     */
    private String generateIssueMessage(QualityRule rule) {
        String ruleType = rule.getRuleType();
        String fieldName = rule.getFieldName() != null ? rule.getFieldName() : rule.getFieldCode();

        switch (ruleType) {
            case "completeness":
                return fieldName + "不能为空";
            case "uniqueness":
                return fieldName + "值重复";
            case "accuracy":
                return fieldName + "格式或值不正确";
            default:
                return rule.getRuleName() + "检查未通过";
        }
    }

    /**
     * 生成任务编码
     */
    private String generateTaskCode() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "QT" + timestamp;
    }

    /**
     * 删除检测任务
     */
    @Transactional
    public void deleteTask(Long id) {
        QualityCheckTask task = taskRepository.findById(id)
                .orElseThrow(() -> new BusinessException("任务不存在"));

        // 删除关联的结果
        resultRepository.deleteByTaskId(id);

        // 删除任务
        taskRepository.delete(task);
    }

    /**
     * 转换任务为DTO
     */
    private QualityCheckTaskDto convertTaskToDto(QualityCheckTask task) {
        QualityCheckTaskDto dto = new QualityCheckTaskDto();
        BeanUtils.copyProperties(task, dto);

        // 填充视图名称
        if (task.getViewId() != null) {
            try {
                ViewDefinitionDto view = viewDefinitionService.getViewDetail(task.getViewId());
                if (view != null) {
                    dto.setViewName(view.getViewName());
                }
            } catch (Exception e) {
                // 忽略
            }
        }

        return dto;
    }

    /**
     * 转换结果为DTO
     */
    private QualityCheckResultDto convertResultToDto(QualityCheckResult result) {
        QualityCheckResultDto dto = new QualityCheckResultDto();
        BeanUtils.copyProperties(result, dto);

        // 填充规则名称
        if (result.getRuleId() != null) {
            ruleRepository.findById(result.getRuleId())
                    .ifPresent(rule -> dto.setRuleName(rule.getRuleName()));
        }

        return dto;
    }

    /**
     * 获取待确认记录列表
     * 按记录ID分组统计检测结果，返回每条记录的检测情况
     */
    public List<Map<String, Object>> getPendingRecords(Long taskId) {
        QualityCheckTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException("任务不存在"));

        // 获取该任务的所有检测结果
        List<QualityCheckResult> results = resultRepository.findByTaskId(taskId);

        // 按记录ID分组统计
        Map<Long, List<QualityCheckResult>> recordMap = results.stream()
                .filter(r -> "main".equals(r.getEntityType())) // 只统计主表
                .collect(Collectors.groupingBy(QualityCheckResult::getRecordId));

        List<Map<String, Object>> pendingRecords = new ArrayList<>();

        for (Map.Entry<Long, List<QualityCheckResult>> entry : recordMap.entrySet()) {
            Long recordId = entry.getKey();
            List<QualityCheckResult> recordResults = entry.getValue();

            int totalRules = recordResults.size();
            int passCount = (int) recordResults.stream().filter(r -> r.getIsPassed() == 1).count();
            int failCount = totalRules - passCount;
            boolean allPassed = failCount == 0;

            // 获取记录名称和编码（从物理表查询）
            String tableName = recordResults.get(0).getTableName();
            Map<String, Object> recordInfo = getRecordInfo(tableName, recordId);

            Map<String, Object> record = new HashMap<>();
            record.put("recordId", recordId);
            record.put("recordCode", recordInfo.get("code"));
            record.put("recordName", recordInfo.get("name"));
            record.put("tableName", tableName);
            record.put("totalRules", totalRules);
            record.put("passCount", passCount);
            record.put("failCount", failCount);
            record.put("allPassed", allPassed);
            record.put("currentStatus", recordInfo.get("status"));

            pendingRecords.add(record);
        }

        return pendingRecords;
    }

    /**
     * 从物理表获取记录基本信息
     */
    private Map<String, Object> getRecordInfo(String tableName, Long recordId) {
        Map<String, Object> info = new HashMap<>();
        info.put("code", "-");
        info.put("name", "-");
        info.put("status", "-");

        try {
            String sql = String.format(
                "SELECT code, name, status FROM %s WHERE id = %d",
                tableName, recordId
            );
            Query query = entityManager.createNativeQuery(sql);
            Object[] result = (Object[]) query.getResultList().stream().findFirst().orElse(null);

            if (result != null && result.length >= 3) {
                info.put("code", result[0] != null ? result[0].toString() : "-");
                info.put("name", result[1] != null ? result[1].toString() : "-");
                info.put("status", result[2] != null ? result[2].toString() : "-");
            }
        } catch (Exception e) {
            log.warn("获取记录信息失败: table={}, id={}", tableName, recordId);
        }

        return info;
    }

    /**
     * 确认记录状态
     * 将记录状态从 PENDING_QC 更改为 ACTIVE_QUALIFIED 或 ACTIVE_UNQUALIFIED
     */
    @Transactional
    public void confirmRecords(Map<String, Object> request, String currentUser) {
        List<Long> recordIds = (List<Long>) request.get("recordIds");
        String tableName = (String) request.get("tableName");
        String targetStatus = (String) request.get("targetStatus");
        Long taskId = request.get("taskId") != null ? Long.valueOf(request.get("taskId").toString()) : null;

        if (recordIds == null || recordIds.isEmpty()) {
            throw new BusinessException("请选择要确认的记录");
        }
        if (tableName == null || tableName.isEmpty()) {
            throw new BusinessException("表名不能为空");
        }
        if (targetStatus == null || !targetStatus.equals("ACTIVE_QUALIFIED") && !targetStatus.equals("ACTIVE_UNQUALIFIED")) {
            throw new BusinessException("目标状态不合法");
        }

        // 批量更新物理表状态
        String sql = String.format(
            "UPDATE %s SET status = '%s', updated_at = NOW() WHERE id IN (%s) AND status = 'PENDING_QC'",
            tableName, targetStatus, recordIds.stream().map(String::valueOf).collect(Collectors.joining(","))
        );

        try {
            Query query = entityManager.createNativeQuery(sql);
            int updated = query.executeUpdate();
            log.info("批量更新记录状态: table={}, count={}, status={}", tableName, updated, targetStatus);

            // 记录操作日志
            if (taskId != null) {
                for (Long recordId : recordIds) {
                    String statusLabel = targetStatus.equals("ACTIVE_QUALIFIED") ? "已生效-合格" : "已生效-不合格";
                    log.info("用户 {} 确认记录 {} 状态为 {}", currentUser, recordId, statusLabel);
                }
            }
        } catch (Exception e) {
            log.error("更新记录状态失败: {}", e.getMessage());
            throw new BusinessException("更新记录状态失败：" + e.getMessage());
        }
    }
}

package com.vueadmin.service.quality;

import com.vueadmin.dto.IssueGroupDto;
import com.vueadmin.dto.IssueItemDto;
import com.vueadmin.dto.QualityIssueDto;
import com.vueadmin.entity.quality.QualityIssue;
import com.vueadmin.entity.standard.ViewEntity;
import com.vueadmin.entity.standard.ViewField;
import com.vueadmin.exception.BusinessException;
import com.vueadmin.repository.QualityIssueRepository;
import com.vueadmin.repository.QualityRuleRepository;
import com.vueadmin.repository.ViewEntityRepository;
import com.vueadmin.repository.ViewFieldRepository;
import com.vueadmin.service.standard.ViewDefinitionService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 问题管理Service
 */
@Slf4j
@Service
public class QualityIssueService {

    @Autowired
    private QualityIssueRepository issueRepository;

    @Autowired
    private QualityRuleRepository ruleRepository;

    @Autowired
    private ViewDefinitionService viewDefinitionService;

    @Autowired
    private ViewEntityRepository viewEntityRepository;

    @Autowired
    private ViewFieldRepository viewFieldRepository;

    @Autowired
    private EntityManager entityManager;

    /**
     * 获取问题列表
     */
    public List<QualityIssueDto> getIssueList(String status, String issueLevel, Long viewId) {
        List<QualityIssue> issues;

        // 处理空字符串，转为 null
        status = (status != null && status.isEmpty()) ? null : status;
        issueLevel = (issueLevel != null && issueLevel.isEmpty()) ? null : issueLevel;

        if (status != null || issueLevel != null || viewId != null) {
            issues = issueRepository.findByFilters(status, issueLevel, viewId);
        } else {
            issues = issueRepository.findAll();
        }

        return issues.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 获取待处理问题列表
     */
    public List<QualityIssueDto> getOpenIssues() {
        List<QualityIssue> issues = issueRepository.findOpenIssues();
        return issues.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 获取问题详情
     */
    public QualityIssueDto getIssueById(Long id) {
        QualityIssue issue = issueRepository.findById(id)
                .orElseThrow(() -> new BusinessException("问题不存在"));
        return convertToDto(issue);
    }

    /**
     * 更新问题状态
     */
    @Transactional
    public QualityIssueDto updateStatus(Long id, String status, String resolution, String resolvedBy) {
        QualityIssue issue = issueRepository.findById(id)
                .orElseThrow(() -> new BusinessException("问题不存在"));

        issue.setStatus(status);

        if ("resolved".equals(status) || "ignored".equals(status) || "closed".equals(status)) {
            issue.setResolvedBy(resolvedBy);
            issue.setResolvedTime(LocalDateTime.now());
            if (resolution != null) {
                issue.setResolution(resolution);
            }
        }

        issue = issueRepository.save(issue);

        // 如果问题已解决或关闭，检查并更新主数据状态
        if ("resolved".equals(status) || "ignored".equals(status) || "closed".equals(status)) {
            updateMasterDataStatus(issue);
        }

        return convertToDto(issue);
    }

    /**
     * 指派问题
     */
    @Transactional
    public QualityIssueDto assignIssue(Long id, String assignee) {
        QualityIssue issue = issueRepository.findById(id)
                .orElseThrow(() -> new BusinessException("问题不存在"));

        issue.setAssignee(assignee);
        issue.setStatus("processing");

        issue = issueRepository.save(issue);
        return convertToDto(issue);
    }

    /**
     * 解决问题
     */
    @Transactional
    public QualityIssueDto resolveIssue(Long id, String resolution, String resolvedBy) {
        return updateStatus(id, "resolved", resolution, resolvedBy);
    }

    /**
     * 忽略问题
     */
    @Transactional
    public QualityIssueDto ignoreIssue(Long id, String reason, String resolvedBy) {
        return updateStatus(id, "ignored", reason, resolvedBy);
    }

    /**
     * 关闭问题
     */
    @Transactional
    public QualityIssueDto closeIssue(Long id, String resolvedBy) {
        return updateStatus(id, "closed", null, resolvedBy);
    }

    /**
     * 批量指派
     */
    @Transactional
    public void batchAssign(List<Long> ids, String assignee) {
        for (Long id : ids) {
            try {
                assignIssue(id, assignee);
            } catch (Exception e) {
                log.error("指派问题失败: id={}", id, e);
            }
        }
    }

    /**
     * 统计问题数量
     */
    public Long countByStatus(String status) {
        return issueRepository.countByStatus(status);
    }

    /**
     * 转换为DTO
     */
    private QualityIssueDto convertToDto(QualityIssue issue) {
        QualityIssueDto dto = new QualityIssueDto();
        BeanUtils.copyProperties(issue, dto);

        // 填充规则名称
        if (issue.getRuleId() != null) {
            ruleRepository.findById(issue.getRuleId())
                    .ifPresent(rule -> dto.setRuleName(rule.getRuleName()));
        }

        // 填充视图名称
        if (issue.getViewId() != null) {
            try {
                var view = viewDefinitionService.getViewDetail(issue.getViewId());
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
     * 更新主数据状态
     * 当问题的所有相关问题都解决后，更新主数据状态为 active
     */
    private void updateMasterDataStatus(QualityIssue issue) {
        try {
            // 只处理主表数据
            if (!"main".equals(issue.getEntityType())) {
                log.debug("子表数据不更新主数据状态");
                return;
            }

            // 获取主记录ID
            Long recordId = issue.getMainRecordId() != null ? issue.getMainRecordId() : issue.getRecordId();
            if (recordId == null) {
                log.warn("问题没有关联的主记录ID: issueId={}", issue.getId());
                return;
            }

            // 获取表名
            String tableName = issue.getTableName();
            if (tableName == null || tableName.isEmpty()) {
                log.warn("问题没有关联的表名: issueId={}", issue.getId());
                return;
            }

            // 检查该主数据的所有问题是否都已解决（包括主表和子表）
            List<QualityIssue> allIssues = issueRepository.findByTableNameAndRecordId(tableName, recordId);
            boolean hasOpenIssues = allIssues.stream()
                    .anyMatch(i -> "open".equals(i.getStatus()) || "processing".equals(i.getStatus()));

            if (!hasOpenIssues) {
                // 所有问题都已解决，更新主数据状态为已生效-合格
                String updateSql = String.format("UPDATE %s SET status = 'ACTIVE_QUALIFIED' WHERE id = ?", tableName);
                Query query = entityManager.createNativeQuery(updateSql);
                query.setParameter(1, recordId);
                int updated = query.executeUpdate();
                log.info("主数据状态更新为已生效-合格: tableName={}, recordId={}, updated={}", tableName, recordId, updated);
            } else {
                log.debug("主数据还有未解决的问题，保持待检状态: tableName={}, recordId={}, openCount={}",
                    tableName, recordId, allIssues.stream().filter(i -> "open".equals(i.getStatus()) || "processing".equals(i.getStatus())).count());
            }
        } catch (Exception e) {
            log.error("更新主数据状态失败: issueId={}", issue.getId(), e);
        }
    }

    /**
     * 获取按记录分组的问题列表
     */
    public List<IssueGroupDto> getGroupedIssues(String status, String issueLevel, Long viewId) {
        List<IssueGroupDto> groups = new ArrayList<>();

        // 处理空字符串
        status = (status != null && status.isEmpty()) ? null : status;
        issueLevel = (issueLevel != null && issueLevel.isEmpty()) ? null : issueLevel;

        // 获取所有问题
        List<QualityIssue> allIssues;
        if (status != null || issueLevel != null || viewId != null) {
            allIssues = issueRepository.findByFilters(status, issueLevel, viewId);
        } else {
            allIssues = issueRepository.findAll();
        }

        // 按 (tableName, mainRecordId 或 recordId) 分组
        Map<String, List<QualityIssue>> groupedMap = allIssues.stream()
            .collect(Collectors.groupingBy(issue -> {
                Long recordId = issue.getMainRecordId() != null ? issue.getMainRecordId() : issue.getRecordId();
                return issue.getTableName() + "_" + recordId;
            }));

        // 转换为分组DTO
        for (Map.Entry<String, List<QualityIssue>> entry : groupedMap.entrySet()) {
            List<QualityIssue> issues = entry.getValue();
            if (issues.isEmpty()) continue;

            QualityIssue first = issues.get(0);
            IssueGroupDto group = new IssueGroupDto();

            // 设置记录信息
            Long recordId = first.getMainRecordId() != null ? first.getMainRecordId() : first.getRecordId();
            group.setRecordId(recordId);
            group.setTableName(first.getTableName());
            group.setViewId(first.getViewId());
            group.setEntityType(first.getEntityType());

            // 获取记录显示名称和编码
            fetchRecordDisplayInfo(group, first.getTableName(), recordId);

            // 获取视图名称
            if (first.getViewId() != null) {
                try {
                    var view = viewDefinitionService.getViewDetail(first.getViewId());
                    if (view != null) {
                        group.setViewName(view.getViewName());
                    }
                } catch (Exception e) {
                    // 忽略
                }
            }

            // 统计问题数量
            long openCount = issues.stream()
                .filter(i -> "open".equals(i.getStatus()) || "processing".equals(i.getStatus()))
                .count();
            long resolvedCount = issues.stream()
                .filter(i -> "resolved".equals(i.getStatus()) || "ignored".equals(i.getStatus()) || "closed".equals(i.getStatus()))
                .count();

            group.setTotalIssues(issues.size());
            group.setOpenIssues((int) openCount);
            group.setResolvedIssues((int) resolvedCount);

            // 转换问题列表
            List<IssueItemDto> issueItems = issues.stream()
                .map(this::convertToItemDto)
                .collect(Collectors.toList());
            group.setIssues(issueItems);

            groups.add(group);
        }

        // 按未解决问题数排序（多的在前）
        groups.sort((a, b) -> b.getOpenIssues().compareTo(a.getOpenIssues()));

        return groups;
    }

    /**
     * 获取记录显示信息（名称、编码、状态）
     */
    private void fetchRecordDisplayInfo(IssueGroupDto group, String tableName, Long recordId) {
        try {
            // 查询记录的基本信息
            // 假设大多数表都有 name 和 code 字段
            String sql = String.format(
                "SELECT name, code, status FROM %s WHERE id = ?", tableName);
            Query query = entityManager.createNativeQuery(sql);
            query.setParameter(1, recordId);

            Object[] result = (Object[]) query.getResultList().stream().findFirst().orElse(null);

            if (result != null) {
                group.setRecordName(result[0] != null ? result[0].toString() : "未知记录");
                group.setRecordCode(result[1] != null ? result[1].toString() : "");
                group.setRecordStatus(result[2] != null ? result[2].toString() : "");
            }
        } catch (Exception e) {
            log.warn("获取记录显示信息失败: tableName={}, recordId={}", tableName, recordId, e);
            group.setRecordName("记录ID: " + recordId);
            group.setRecordCode("");
            group.setRecordStatus("");
        }
    }

    /**
     * 转换为问题项DTO
     */
    private IssueItemDto convertToItemDto(QualityIssue issue) {
        IssueItemDto dto = new IssueItemDto();
        dto.setId(issue.getId());
        dto.setIssueCode(issue.getIssueCode());
        dto.setFieldCode(issue.getFieldCode());
        dto.setFieldValue(issue.getFieldValue());
        dto.setIssueType(issue.getIssueType());
        dto.setIssueLevel(issue.getIssueLevel());
        dto.setIssueDesc(issue.getIssueDesc());
        dto.setStatus(issue.getStatus());
        dto.setTableName(issue.getTableName());
        dto.setRecordId(issue.getRecordId());
        dto.setEntityId(issue.getEntityId());
        dto.setEntityType(issue.getEntityType());

        // 获取字段名称和类型
        if (issue.getViewId() != null && issue.getFieldCode() != null) {
            try {
                // 通过视图ID获取实体列表
                List<ViewEntity> entities = viewEntityRepository.findByViewId(issue.getViewId());
                for (ViewEntity entity : entities) {
                    // 获取实体的字段
                    List<ViewField> fields = viewFieldRepository.findByEntityIdOrderBySort(entity.getId());
                    for (ViewField field : fields) {
                        if (issue.getFieldCode().equals(field.getFieldCode())) {
                            dto.setFieldName(field.getFieldName());
                            dto.setFieldType(field.getFieldType());
                            dto.setDomainCode(field.getDomainCode());
                            break;
                        }
                    }
                    if (dto.getFieldName() != null) break;
                }
            } catch (Exception e) {
                // 忽略
            }
        }

        // 如果没有找到字段名，使用字段编码
        if (dto.getFieldName() == null || dto.getFieldName().isEmpty()) {
            dto.setFieldName(issue.getFieldCode());
        }

        return dto;
    }

    /**
     * 修改字段值并解决问题
     */
    @Transactional
    public IssueItemDto updateFieldAndResolve(Long issueId, String newValue, String resolvedBy) {
        QualityIssue issue = issueRepository.findById(issueId)
            .orElseThrow(() -> new BusinessException("问题不存在"));

        String tableName = issue.getTableName();
        String fieldCode = issue.getFieldCode();
        Long recordId = issue.getRecordId();

        if (tableName == null || fieldCode == null || recordId == null) {
            throw new BusinessException("问题数据不完整，无法修改");
        }

        try {
            // 1. 更新主数据字段值
            String updateSql = String.format("UPDATE %s SET %s = ? WHERE id = ?", tableName, fieldCode);
            Query query = entityManager.createNativeQuery(updateSql);
            query.setParameter(1, newValue);
            query.setParameter(2, recordId);
            int updated = query.executeUpdate();

            if (updated == 0) {
                throw new BusinessException("更新记录失败，记录可能不存在");
            }

            log.info("字段值更新成功: table={}, recordId={}, field={}, newValue={}", tableName, recordId, fieldCode, newValue);

            // 2. 重新校验新值是否符合规则
            boolean isValid = validateFieldValue(issue, newValue);
            if (!isValid) {
                // 校验不通过，不解决该问题
                log.warn("字段值校验不通过: issueId={}, newValue={}", issueId, newValue);
                throw new BusinessException("新值不符合规则要求，请检查输入");
            }

            // 3. 校验通过，更新问题状态为已解决
            issue.setStatus("resolved");
            issue.setResolvedBy(resolvedBy);
            issue.setResolvedTime(LocalDateTime.now());
            issue.setResolution("已修改字段值: " + newValue);
            issue = issueRepository.save(issue);

            // 4. 检查并更新记录状态
            updateMasterDataStatus(issue);

            return convertToItemDto(issue);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("修改字段值失败: issueId={}", issueId, e);
            throw new BusinessException("修改字段值失败: " + e.getMessage());
        }
    }

    /**
     * 校验字段值是否符合规则
     */
    private boolean validateFieldValue(QualityIssue issue, String newValue) {
        try {
            // 获取规则信息
            if (issue.getRuleId() == null) {
                return true; // 没有规则，默认通过
            }

            var ruleOpt = ruleRepository.findById(issue.getRuleId());
            if (ruleOpt.isEmpty()) {
                return true;
            }

            var rule = ruleOpt.get();
            String ruleType = rule.getRuleType();
            String checkType = rule.getCheckType();
            String checkConfig = rule.getCheckConfig();
            String fieldName = rule.getFieldName();

            log.info("校验字段值: field={}, ruleType={}, checkType={}, newValue={}", fieldName, ruleType, checkType, newValue);

            // 根据规则类型校验
            switch (ruleType) {
                case "completeness":
                    // 完整性检查：非空
                    return newValue != null && !newValue.trim().isEmpty();

                case "accuracy":
                    // 准确性检查
                    boolean result = validateAccuracy(newValue, checkType, checkConfig, fieldName);
                    log.info("准确性校验结果: {}", result);
                    return result;

                default:
                    return true;
            }
        } catch (Exception e) {
            log.error("校验字段值失败: issueId={}", issue.getId(), e);
            return true; // 异常时默认通过
        }
    }

    /**
     * 准确性校验
     */
    private boolean validateAccuracy(String value, String checkType, String checkConfig, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            return true; // 空值不校验
        }

        // 根据字段名判断校验类型
        if (fieldName != null) {
            String lowerFieldName = fieldName.toLowerCase();
            if (lowerFieldName.contains("邮箱") || lowerFieldName.contains("email")) {
                // 邮箱校验
                boolean valid = value.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
                log.info("邮箱校验: value={}, valid={}", value, valid);
                return valid;
            }
            if (lowerFieldName.contains("电话") || lowerFieldName.contains("手机") || lowerFieldName.contains("phone")) {
                // 电话号码校验
                boolean valid = value.matches("^1[3-9]\\d{9}$") || value.matches("^\\d{3,4}-?\\d{7,8}$");
                log.info("电话校验: value={}, valid={}", value, valid);
                return valid;
            }
        }

        // 根据checkType校验
        if (checkType == null) {
            return true;
        }

        switch (checkType) {
            case "regex":
                // 正则表达式校验
                if (checkConfig != null && checkConfig.contains("\"pattern\":")) {
                    try {
                        String pattern = checkConfig.split("\"pattern\":\"")[1].split("\"")[0];
                        return value.matches(pattern);
                    } catch (Exception e) {
                        return true;
                    }
                }
                return true;

            case "phone":
                // 电话号码校验
                return value.matches("^1[3-9]\\d{9}$") || value.matches("^\\d{3,4}-?\\d{7,8}$");

            case "email":
                // 邮箱校验
                return value.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

            default:
                return true;
        }
    }
}

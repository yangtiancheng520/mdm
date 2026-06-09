package com.vueadmin.service.quality;

import com.vueadmin.dto.QualityRuleDto;
import com.vueadmin.dto.ViewDefinitionDto;
import com.vueadmin.dto.ViewEntityDto;
import com.vueadmin.dto.ViewFieldDto;
import com.vueadmin.entity.quality.QualityRule;
import com.vueadmin.entity.quality.QualityRuleTemplate;
import com.vueadmin.exception.BusinessException;
import com.vueadmin.repository.QualityRuleRepository;
import com.vueadmin.repository.QualityRuleTemplateRepository;
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
import java.math.BigInteger;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 质量规则Service
 */
@Slf4j
@Service
public class QualityRuleService {

    @Autowired
    private QualityRuleRepository qualityRuleRepository;

    @Autowired
    private QualityRuleTemplateRepository templateRepository;

    @Autowired
    private ViewDefinitionService viewDefinitionService;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 获取规则列表
     */
    public List<QualityRuleDto> getRuleList(Long viewId, Long entityId, String ruleType, String status) {
        List<QualityRule> rules;

        if (viewId != null) {
            if (status != null && !status.isEmpty()) {
                rules = qualityRuleRepository.findByViewIdAndStatus(viewId, status);
            } else {
                rules = qualityRuleRepository.findByViewId(viewId);
            }
        } else if (entityId != null) {
            if (status != null && !status.isEmpty()) {
                rules = qualityRuleRepository.findByEntityIdAndStatus(entityId, status);
            } else {
                rules = qualityRuleRepository.findByEntityId(entityId);
            }
        } else if (status != null && !status.isEmpty()) {
            rules = qualityRuleRepository.findByStatus(status);
        } else {
            rules = qualityRuleRepository.findAll();
        }

        // 过滤规则类型
        if (ruleType != null && !ruleType.isEmpty()) {
            rules = rules.stream()
                    .filter(r -> ruleType.equals(r.getRuleType()))
                    .collect(Collectors.toList());
        }

        // 批量加载视图信息，避免N+1查询
        Map<Long, ViewDefinitionDto> viewMap = new HashMap<>();
        Set<Long> viewIds = rules.stream()
                .map(QualityRule::getViewId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        for (Long vid : viewIds) {
            try {
                ViewDefinitionDto view = viewDefinitionService.getViewDetail(vid);
                if (view != null) {
                    viewMap.put(vid, view);
                }
            } catch (Exception e) {
                log.warn("加载视图信息失败: {}", vid);
            }
        }

        final Map<Long, ViewDefinitionDto> finalViewMap = viewMap;
        return rules.stream()
                .map(rule -> convertToDto(rule, finalViewMap))
                .collect(Collectors.toList());
    }

    /**
     * 获取规则详情
     */
    public QualityRuleDto getRuleById(Long id) {
        QualityRule rule = qualityRuleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("规则不存在"));
        return convertToDto(rule);
    }

    /**
     * 创建规则
     */
    @Transactional
    public QualityRuleDto createRule(QualityRuleDto dto) {
        // 验证规则编码唯一
        if (qualityRuleRepository.existsByRuleCode(dto.getRuleCode())) {
            throw new BusinessException("规则编码已存在");
        }

        // 填充实体和字段信息
        fillEntityAndFieldInfo(dto);

        QualityRule rule = new QualityRule();
        BeanUtils.copyProperties(dto, rule);

        if (rule.getThreshold() == null) {
            rule.setThreshold(new BigDecimal("100.00"));
        }
        if (rule.getSeverity() == null) {
            rule.setSeverity("warning");
        }
        if (rule.getStatus() == null) {
            rule.setStatus("active");
        }

        rule = qualityRuleRepository.save(rule);
        return convertToDto(rule);
    }

    /**
     * 更新规则
     */
    @Transactional
    public QualityRuleDto updateRule(Long id, QualityRuleDto dto) {
        QualityRule rule = qualityRuleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("规则不存在"));

        // 检查规则编码是否重复
        if (!rule.getRuleCode().equals(dto.getRuleCode()) &&
                qualityRuleRepository.existsByRuleCode(dto.getRuleCode())) {
            throw new BusinessException("规则编码已存在");
        }

        // 填充实体和字段信息
        fillEntityAndFieldInfo(dto);

        BeanUtils.copyProperties(dto, rule, "id", "createdAt", "createdBy");

        rule = qualityRuleRepository.save(rule);
        return convertToDto(rule);
    }

    /**
     * 删除规则
     */
    @Transactional
    public void deleteRule(Long id) {
        QualityRule rule = qualityRuleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("规则不存在"));
        qualityRuleRepository.delete(rule);
    }

    /**
     * 批量删除规则
     */
    @Transactional
    public void batchDeleteRules(List<Long> ids) {
        List<QualityRule> rules = qualityRuleRepository.findAllById(ids);
        if (rules.size() != ids.size()) {
            throw new BusinessException("部分规则不存在");
        }
        qualityRuleRepository.deleteAll(rules);
    }

    /**
     * 启用/停用规则
     */
    @Transactional
    public QualityRuleDto toggleStatus(Long id) {
        QualityRule rule = qualityRuleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("规则不存在"));

        rule.setStatus("active".equals(rule.getStatus()) ? "inactive" : "active");
        rule = qualityRuleRepository.save(rule);
        return convertToDto(rule);
    }

    /**
     * 获取视图的实体和字段信息
     */
    public ViewDefinitionDto getViewWithEntities(Long viewId) {
        return viewDefinitionService.getViewDetail(viewId);
    }

    /**
     * 根据实体ID获取规则列表
     */
    public List<QualityRuleDto> getRulesByEntityIds(List<Long> entityIds) {
        List<QualityRule> rules = qualityRuleRepository.findByEntityIds(entityIds);
        return rules.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 填充实体和字段信息
     */
    private void fillEntityAndFieldInfo(QualityRuleDto dto) {
        if (dto.getViewId() == null || dto.getEntityId() == null) {
            return;
        }

        ViewDefinitionDto view = viewDefinitionService.getViewDetail(dto.getViewId());
        if (view == null) {
            throw new BusinessException("视图不存在");
        }

        // 查找对应的实体
        ViewEntityDto entity = view.getEntities().stream()
                .filter(e -> dto.getEntityId().equals(e.getId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException("实体不存在"));

        dto.setEntityName(entity.getEntityName());
        dto.setEntityType(entity.getEntityType());
        dto.setTableName(entity.getTableName());

        // 如果指定了字段ID，填充字段信息
        if (dto.getFieldId() != null && entity.getFields() != null) {
            ViewFieldDto field = entity.getFields().stream()
                    .filter(f -> dto.getFieldId().equals(f.getId()))
                    .findFirst()
                    .orElse(null);

            if (field != null) {
                dto.setFieldCode(field.getFieldCode());
                dto.setFieldName(field.getFieldName());
            }
        }
    }

    /**
     * 转换为DTO
     */
    private QualityRuleDto convertToDto(QualityRule rule) {
        return convertToDto(rule, null);
    }

    /**
     * 转换为DTO（带视图缓存）
     */
    private QualityRuleDto convertToDto(QualityRule rule, Map<Long, ViewDefinitionDto> viewMap) {
        QualityRuleDto dto = new QualityRuleDto();
        BeanUtils.copyProperties(rule, dto);

        // 填充视图和实体名称
        if (rule.getViewId() != null) {
            try {
                ViewDefinitionDto view = null;
                if (viewMap != null) {
                    view = viewMap.get(rule.getViewId());
                } else {
                    view = viewDefinitionService.getViewDetail(rule.getViewId());
                }

                if (view != null) {
                    dto.setViewName(view.getViewName());

                    // 查找实体名称
                    if (rule.getEntityId() != null && view.getEntities() != null) {
                        view.getEntities().stream()
                                .filter(e -> rule.getEntityId().equals(e.getId()))
                                .findFirst()
                                .ifPresent(e -> dto.setEntityName(e.getEntityName()));
                    }
                }
            } catch (Exception e) {
                // 忽略错误
            }
        }

        return dto;
    }

    /**
     * 测试规则（只检测前100条数据）
     */
    public Map<String, Object> testRule(QualityRuleDto dto) {
        Map<String, Object> result = new HashMap<>();

        try {
            String tableName = dto.getTableName();
            String fieldCode = dto.getFieldCode();

            if (tableName == null || tableName.isEmpty() || fieldCode == null || fieldCode.isEmpty()) {
                result.put("success", false);
                result.put("error", "表名或字段编码不能为空");
                return result;
            }

            // 查询前100条数据
            String sql = String.format(
                "SELECT id, %s FROM %s WHERE status = 'active' LIMIT 100",
                fieldCode, tableName
            );

            Query query = entityManager.createNativeQuery(sql);
            @SuppressWarnings("unchecked")
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
                        Map<String, Object> record = new HashMap<>();
                        record.put("recordId", row[0]);
                        record.put("fieldValue", row[1] != null ? row[1].toString() : null);
                        record.put("reason", generateIssueMessage(dto));
                        failRecords.add(record);
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
     * 应用模板到规则
     */
    public QualityRuleDto applyTemplate(Long templateId, QualityRuleDto dto) {
        QualityRuleTemplate template = templateRepository.findById(templateId)
            .orElseThrow(() -> new BusinessException("模板不存在"));

        dto.setCheckType(template.getCheckType());
        dto.setCheckConfig(template.getCheckConfig());
        dto.setSeverity(template.getSeverity());
        dto.setTemplateId(templateId);

        return dto;
    }

    /**
     * 执行单条数据检查
     */
    private boolean executeSingleCheck(Object[] row, QualityRuleDto rule) {
        String checkType = rule.getCheckType();
        String checkConfig = rule.getCheckConfig();
        Object fieldValue = row[1];

        if (checkType == null || checkType.isEmpty()) {
            return fieldValue != null;
        }

        if (checkConfig == null || checkConfig.isEmpty()) {
            return executeDefaultCheck(fieldValue, checkType);
        }

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> config = objectMapper.readValue(checkConfig, Map.class);
            String actualCheckType = (String) config.getOrDefault("checkType", checkType);

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
                default:
                    return true;
            }
        } catch (Exception e) {
            log.error("解析检查配置失败", e);
            return executeDefaultCheck(fieldValue, checkType);
        }
    }

    /**
     * 非空检查
     */
    private boolean checkNotNull(Object fieldValue, Map<String, Object> config) {
        if (fieldValue == null) return false;
        boolean trimWhitespace = Boolean.parseBoolean(config.getOrDefault("trimWhitespace", "true").toString());
        if (trimWhitespace && fieldValue instanceof String) {
            return !((String) fieldValue).trim().isEmpty();
        }
        return true;
    }

    /**
     * 非空字符串检查
     */
    private boolean checkNotEmpty(Object fieldValue, Map<String, Object> config) {
        if (fieldValue == null) return false;
        String strValue = fieldValue.toString();
        int minLength = Integer.parseInt(config.getOrDefault("minLength", "1").toString());
        Object maxLengthObj = config.get("maxLength");
        int length = strValue.length();
        if (length < minLength) return false;
        if (maxLengthObj != null) {
            int maxLength = Integer.parseInt(maxLengthObj.toString());
            if (length > maxLength) return false;
        }
        return true;
    }

    /**
     * 正则表达式检查
     */
    private boolean checkRegex(Object fieldValue, Map<String, Object> config) {
        if (fieldValue == null) return true;
        String pattern = (String) config.get("pattern");
        if (pattern == null || pattern.isEmpty()) return true;
        return Pattern.matches(pattern, fieldValue.toString());
    }

    /**
     * 长度检查
     */
    private boolean checkLength(Object fieldValue, Map<String, Object> config) {
        if (fieldValue == null) return true;
        String strValue = fieldValue.toString();
        Object minLengthObj = config.get("minLength");
        Object maxLengthObj = config.get("maxLength");
        String countType = (String) config.getOrDefault("countType", "char");
        int length = "byte".equals(countType) ? strValue.getBytes().length : strValue.length();
        if (minLengthObj != null && length < Integer.parseInt(minLengthObj.toString())) return false;
        if (maxLengthObj != null && length > Integer.parseInt(maxLengthObj.toString())) return false;
        return true;
    }

    /**
     * 范围检查
     */
    private boolean checkRange(Object fieldValue, Map<String, Object> config) {
        if (fieldValue == null) return true;
        try {
            BigDecimal numValue = new BigDecimal(fieldValue.toString());
            Object minValueObj = config.get("minValue");
            Object maxValueObj = config.get("maxValue");
            String minOperator = (String) config.getOrDefault("minOperator", ">=");
            String maxOperator = (String) config.getOrDefault("maxOperator", "<=");

            boolean minPass = minValueObj == null ||
                (">=".equals(minOperator) ? numValue.compareTo(new BigDecimal(minValueObj.toString())) >= 0
                 : numValue.compareTo(new BigDecimal(minValueObj.toString())) > 0);
            boolean maxPass = maxValueObj == null ||
                ("<=".equals(maxOperator) ? numValue.compareTo(new BigDecimal(maxValueObj.toString())) <= 0
                 : numValue.compareTo(new BigDecimal(maxValueObj.toString())) < 0);

            return minPass && maxPass;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 值域检查
     */
    private boolean checkDomain(Object fieldValue, Map<String, Object> config) {
        if (fieldValue == null) {
            return Boolean.parseBoolean(config.getOrDefault("allowNull", "true").toString());
        }

        String domainCode = (String) config.get("domainCode");
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
     * 默认检查
     */
    private boolean executeDefaultCheck(Object fieldValue, String checkType) {
        if ("not_null".equals(checkType) || "completeness".equals(checkType)) {
            return fieldValue != null;
        }
        return true;
    }

    /**
     * 生成问题描述
     */
    private String generateIssueMessage(QualityRuleDto rule) {
        String fieldName = rule.getFieldName() != null ? rule.getFieldName() : rule.getFieldCode();
        String checkType = rule.getCheckType() != null ? rule.getCheckType() : "not_null";

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
            default:
                return rule.getRuleName() + "检查未通过";
        }
    }
}

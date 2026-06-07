package com.vueadmin.service.standard;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vueadmin.dto.EncodingRuleDto;
import com.vueadmin.dto.PageResult;
import com.vueadmin.entity.standard.EncodingRule;
import com.vueadmin.repository.EncodingRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 编码规则服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EncodingRuleService {

    private final EncodingRuleRepository ruleRepository;
    private final ObjectMapper objectMapper;

    /**
     * 搜索规则
     */
    public PageResult<EncodingRuleDto> search(String ruleCode, String ruleName, String status, int page, int size) {
        Specification<EncodingRule> spec = (root, query, cb) -> {
            var predicates = cb.conjunction();
            if (ruleCode != null && !ruleCode.isEmpty()) {
                predicates = cb.and(predicates, cb.like(root.get("ruleCode"), "%" + ruleCode + "%"));
            }
            if (ruleName != null && !ruleName.isEmpty()) {
                predicates = cb.and(predicates, cb.like(root.get("ruleName"), "%" + ruleName + "%"));
            }
            if (status != null && !status.isEmpty()) {
                predicates = cb.and(predicates, cb.equal(root.get("status"), status));
            }
            return predicates;
        };

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<EncodingRule> pageResult = ruleRepository.findAll(spec, pageable);

        List<EncodingRuleDto> list = pageResult.getContent().stream()
                .map(this::toDto)
                .toList();

        return new PageResult<>(list, pageResult.getTotalElements());
    }

    /**
     * 获取所有启用的规则
     */
    public List<EncodingRuleDto> getActiveRules() {
        return ruleRepository.findByStatus("active").stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * 根据ID获取规则
     */
    public EncodingRuleDto getById(Long id) {
        return ruleRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new IllegalArgumentException("编码规则不存在: " + id));
    }

    /**
     * 根据编码获取规则
     */
    public EncodingRuleDto getByCode(String ruleCode) {
        return ruleRepository.findByRuleCode(ruleCode)
                .map(this::toDto)
                .orElseThrow(() -> new IllegalArgumentException("编码规则不存在: " + ruleCode));
    }

    /**
     * 创建规则
     */
    @Transactional
    public EncodingRuleDto create(EncodingRuleDto dto) {
        // 检查编码是否重复
        if (ruleRepository.existsByRuleCode(dto.getRuleCode())) {
            throw new IllegalArgumentException("规则编码已存在: " + dto.getRuleCode());
        }

        EncodingRule rule = new EncodingRule();
        rule.setRuleCode(dto.getRuleCode());
        rule.setRuleName(dto.getRuleName());
        rule.setScopeType(dto.getScopeType());
        rule.setScopeConfig(dto.getScopeConfig());
        rule.setStatus(dto.getStatus() != null ? dto.getStatus() : "active");
        rule.setExample(dto.getExample());
        rule.setDescription(dto.getDescription());
        rule.setCreatedBy(dto.getCreatedBy());

        // 序列化规则定义
        if (dto.getRuleDefinition() != null) {
            try {
                rule.setRuleDefinition(objectMapper.writeValueAsString(dto.getRuleDefinition()));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("序列化规则定义失败", e);
            }
        }

        rule = ruleRepository.save(rule);
        log.info("创建编码规则成功: {}", rule.getRuleCode());
        return toDto(rule);
    }

    /**
     * 更新规则
     */
    @Transactional
    public EncodingRuleDto update(Long id, EncodingRuleDto dto) {
        EncodingRule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("编码规则不存在: " + id));

        // 检查编码是否重复（排除自己）
        Optional<EncodingRule> existing = ruleRepository.findByRuleCode(dto.getRuleCode());
        if (existing.isPresent() && !existing.get().getId().equals(id)) {
            throw new IllegalArgumentException("规则编码已存在: " + dto.getRuleCode());
        }

        rule.setRuleCode(dto.getRuleCode());
        rule.setRuleName(dto.getRuleName());
        rule.setScopeType(dto.getScopeType());
        rule.setScopeConfig(dto.getScopeConfig());
        rule.setStatus(dto.getStatus());
        rule.setExample(dto.getExample());
        rule.setDescription(dto.getDescription());
        rule.setUpdatedBy(dto.getUpdatedBy());

        // 序列化规则定义
        if (dto.getRuleDefinition() != null) {
            try {
                rule.setRuleDefinition(objectMapper.writeValueAsString(dto.getRuleDefinition()));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("序列化规则定义失败", e);
            }
        }

        rule = ruleRepository.save(rule);
        log.info("更新编码规则成功: {}", rule.getRuleCode());
        return toDto(rule);
    }

    /**
     * 删除规则
     */
    @Transactional
    public void delete(Long id) {
        EncodingRule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("编码规则不存在: " + id));

        ruleRepository.delete(rule);
        log.info("删除编码规则成功: {}", rule.getRuleCode());
    }

    /**
     * 批量删除
     */
    @Transactional
    public void batchDelete(List<Long> ids) {
        ruleRepository.deleteAllById(ids);
        log.info("批量删除编码规则成功: {} 条", ids.size());
    }

    /**
     * 转换为DTO
     */
    private EncodingRuleDto toDto(EncodingRule rule) {
        EncodingRuleDto dto = new EncodingRuleDto();
        dto.setId(rule.getId());
        dto.setRuleCode(rule.getRuleCode());
        dto.setRuleName(rule.getRuleName());
        dto.setScopeType(rule.getScopeType());
        dto.setScopeConfig(rule.getScopeConfig());
        dto.setStatus(rule.getStatus());
        dto.setExample(rule.getExample());
        dto.setDescription(rule.getDescription());
        dto.setCreatedBy(rule.getCreatedBy());
        dto.setCreatedAt(rule.getCreatedAt());
        dto.setUpdatedBy(rule.getUpdatedBy());
        dto.setUpdatedAt(rule.getUpdatedAt());

        // 反序列化规则定义
        if (rule.getRuleDefinition() != null) {
            try {
                dto.setRuleDefinition(objectMapper.readValue(rule.getRuleDefinition(), EncodingRuleDto.RuleDefinition.class));
            } catch (JsonProcessingException e) {
                log.error("反序列化规则定义失败: {}", e.getMessage());
            }
        }

        return dto;
    }
}

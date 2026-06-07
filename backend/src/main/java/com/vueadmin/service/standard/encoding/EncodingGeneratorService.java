package com.vueadmin.service.standard.encoding;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vueadmin.dto.EncodingRuleDto;
import com.vueadmin.entity.standard.EncodingRule;
import com.vueadmin.repository.EncodingRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 编码生成服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EncodingGeneratorService {

    private final EncodingRuleRepository ruleRepository;
    private final Map<String, SegmentResolver> resolvers;
    private final ObjectMapper objectMapper;

    /**
     * 生成编码
     *
     * @param ruleCode     规则编码
     * @param businessData 业务数据
     * @return 生成的编码
     */
    @Transactional
    public String generate(String ruleCode, Map<String, Object> businessData) {
        // 1. 获取规则定义
        EncodingRule rule = ruleRepository.findByRuleCode(ruleCode)
                .orElseThrow(() -> new IllegalArgumentException("编码规则不存在: " + ruleCode));

        if (!"active".equals(rule.getStatus())) {
            throw new IllegalStateException("编码规则已停用: " + ruleCode);
        }

        // 2. 解析规则定义
        EncodingRuleDto.RuleDefinition definition;
        try {
            definition = objectMapper.readValue(rule.getRuleDefinition(), EncodingRuleDto.RuleDefinition.class);
        } catch (Exception e) {
            throw new RuntimeException("解析规则定义失败: " + e.getMessage(), e);
        }

        // 3. 构建上下文
        EncodingContext context = new EncodingContext(businessData);
        context.setRuleId(rule.getId());

        // 计算范围键
        String scopeKey = buildScopeKey(definition.getSettings(), businessData);
        context.setScopeKey(scopeKey);

        // 4. 逐段解析
        StringBuilder code = new StringBuilder();
        List<EncodingRuleDto.SegmentConfig> segments = definition.getSegments();
        if (segments != null) {
            for (EncodingRuleDto.SegmentConfig segment : segments) {
                String type = segment.getType();
                SegmentResolver resolver = resolvers.get(type + "Resolver");
                if (resolver == null) {
                    log.warn("未找到规则段解析器: {}", type);
                    continue;
                }

                String value = resolver.resolve(segment.getConfig(), context);
                code.append(value);
                context.addSegment(value);
            }
        }

        // 5. 校验长度
        String result = code.toString();
        EncodingRuleDto.Settings settings = definition.getSettings();
        if (settings != null && settings.getMaxLength() != null) {
            if (result.length() > settings.getMaxLength()) {
                throw new IllegalStateException("生成的编码超过最大长度限制: " + settings.getMaxLength());
            }
        }

        log.info("生成编码成功: ruleCode={}, result={}", ruleCode, result);
        return result;
    }

    /**
     * 预览编码（不实际生成序列号）
     *
     * @param ruleCode     规则编码
     * @param businessData 业务数据
     * @return 预览编码
     */
    public String preview(String ruleCode, Map<String, Object> businessData) {
        // 获取规则定义
        EncodingRule rule = ruleRepository.findByRuleCode(ruleCode)
                .orElseThrow(() -> new IllegalArgumentException("编码规则不存在: " + ruleCode));

        // 解析规则定义
        EncodingRuleDto.RuleDefinition definition;
        try {
            definition = objectMapper.readValue(rule.getRuleDefinition(), EncodingRuleDto.RuleDefinition.class);
        } catch (Exception e) {
            throw new RuntimeException("解析规则定义失败: " + e.getMessage(), e);
        }

        // 构建上下文
        EncodingContext context = new EncodingContext(businessData);
        context.setRuleId(rule.getId());

        // 逐段解析（序列号使用预览值）
        StringBuilder code = new StringBuilder();
        List<EncodingRuleDto.SegmentConfig> segments = definition.getSegments();
        if (segments != null) {
            for (EncodingRuleDto.SegmentConfig segment : segments) {
                String type = segment.getType();
                SegmentResolver resolver = resolvers.get(type + "Resolver");

                if (resolver == null) {
                    continue;
                }

                // 序列号类型使用预览值
                if ("sequence".equals(type)) {
                    Map<String, Object> config = new HashMap<>(segment.getConfig());
                    config.put("_preview", true);
                    String value = resolver.resolve(config, context);
                    code.append(value);
                    context.addSegment(value);
                } else {
                    String value = resolver.resolve(segment.getConfig(), context);
                    code.append(value);
                    context.addSegment(value);
                }
            }
        }

        return code.toString();
    }

    /**
     * 构建范围键
     */
    private String buildScopeKey(EncodingRuleDto.Settings settings, Map<String, Object> businessData) {
        if (settings == null || settings.getScopeFields() == null || settings.getScopeFields().isEmpty()) {
            return "default";
        }

        StringBuilder key = new StringBuilder();
        for (String field : settings.getScopeFields()) {
            Object value = businessData.get(field);
            if (key.length() > 0) {
                key.append("|");
            }
            key.append(value != null ? String.valueOf(value) : "");
        }
        return key.toString();
    }
}

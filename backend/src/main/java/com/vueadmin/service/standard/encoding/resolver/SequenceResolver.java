package com.vueadmin.service.standard.encoding.resolver;

import com.vueadmin.entity.standard.EncodingSequence;
import com.vueadmin.repository.EncodingSequenceRepository;
import com.vueadmin.service.standard.encoding.EncodingContext;
import com.vueadmin.service.standard.encoding.SegmentResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Map;

/**
 * 序列号解析器
 */
@Component
@RequiredArgsConstructor
public class SequenceResolver implements SegmentResolver {

    private final EncodingSequenceRepository sequenceRepository;

    @Override
    @Transactional
    public String resolve(Map<String, Object> config, EncodingContext context) {
        // 获取配置
        Integer length = getConfigValue(config, "length", 6);
        String padding = getConfigValue(config, "padding", "0");
        Long start = getConfigValue(config, "start", 1L);
        String resetCycle = getConfigValue(config, "resetCycle", "never");

        // 获取序列号范围键
        String tempScopeKey = context.getScopeKey();
        final String scopeKey = (tempScopeKey == null || tempScopeKey.isEmpty()) ? "default" : tempScopeKey;

        // 获取或创建序列记录（加锁）
        EncodingSequence sequence = sequenceRepository
                .findByRuleIdAndScopeKey(context.getRuleId(), scopeKey)
                .orElseGet(() -> createSequence(context.getRuleId(), scopeKey, start, resetCycle));

        // 检查是否需要重置
        if (needReset(sequence, resetCycle)) {
            sequence.setCurrentValue(0L);
            sequence.setResetDate(LocalDate.now());
        }

        // 递增序列号
        Long nextValue = sequence.getCurrentValue() + 1;
        sequence.setCurrentValue(nextValue);
        sequenceRepository.save(sequence);

        // 格式化输出
        return formatSequence(nextValue, length, padding);
    }

    /**
     * 创建序列记录
     */
    private EncodingSequence createSequence(Long ruleId, String scopeKey, Long start, String resetCycle) {
        EncodingSequence sequence = new EncodingSequence();
        sequence.setRuleId(ruleId);
        sequence.setScopeKey(scopeKey);
        sequence.setCurrentValue(start - 1); // 第一次获取时会+1
        sequence.setResetCycle(resetCycle);
        sequence.setResetDate(LocalDate.now());
        return sequenceRepository.save(sequence);
    }

    /**
     * 检查是否需要重置
     */
    private boolean needReset(EncodingSequence sequence, String resetCycle) {
        if ("never".equals(resetCycle) || sequence.getResetDate() == null) {
            return false;
        }

        LocalDate lastReset = sequence.getResetDate();
        LocalDate today = LocalDate.now();

        return switch (resetCycle) {
            case "daily" -> !lastReset.equals(today);
            case "weekly" -> !lastReset.plusWeeks(1).isAfter(today);
            case "monthly" -> !lastReset.with(TemporalAdjusters.firstDayOfMonth())
                    .equals(today.with(TemporalAdjusters.firstDayOfMonth()));
            case "yearly" -> lastReset.getYear() != today.getYear();
            default -> false;
        };
    }

    /**
     * 格式化序列号
     */
    private String formatSequence(Long value, Integer length, String padding) {
        String str = String.valueOf(value);
        if (str.length() >= length) {
            return str.substring(0, length);
        }
        return String.format("%" + length + "s", str).replace(" ", padding);
    }

    @SuppressWarnings("unchecked")
    private <T> T getConfigValue(Map<String, Object> config, String key, T defaultValue) {
        Object value = config.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (defaultValue instanceof Integer && value instanceof Number) {
            return (T) Integer.valueOf(((Number) value).intValue());
        }
        if (defaultValue instanceof Long && value instanceof Number) {
            return (T) Long.valueOf(((Number) value).longValue());
        }
        return (T) value;
    }

    @Override
    public String getType() {
        return "sequence";
    }
}

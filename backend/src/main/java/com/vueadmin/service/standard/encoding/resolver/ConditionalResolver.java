package com.vueadmin.service.standard.encoding.resolver;

import com.vueadmin.service.standard.encoding.EncodingContext;
import com.vueadmin.service.standard.encoding.SegmentResolver;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 条件值解析器
 * 根据业务字段的值返回不同的编码段
 */
@Component
public class ConditionalResolver implements SegmentResolver {

    @Override
    @SuppressWarnings("unchecked")
    public String resolve(Map<String, Object> config, EncodingContext context) {
        // 获取输出长度和填充字符
        Integer length = config.get("length") != null ? ((Number) config.get("length")).intValue() : null;
        String padding = config.get("padding") != null ? String.valueOf(config.get("padding")) : "0";
        char padChar = padding.isEmpty() ? '0' : padding.charAt(0);

        // 获取条件字段
        String field = (String) config.get("field");
        if (field == null) {
            return padResult("", length, padChar);
        }

        // 获取字段值
        Object fieldValue = context.getFieldValue(field);
        String fieldValueStr = fieldValue != null ? String.valueOf(fieldValue) : "";

        // 获取规则列表
        Object rulesObj = config.get("rules");
        if (rulesObj instanceof List) {
            List<Map<String, Object>> rules = (List<Map<String, Object>>) rulesObj;
            for (Map<String, Object> rule : rules) {
                String when = (String) rule.get("when");
                if (when != null && when.equals(fieldValueStr)) {
                    String result = String.valueOf(rule.getOrDefault("then", ""));
                    return padResult(result, length, padChar);
                }
            }
        }

        // 返回默认值（支持 defaultValue 和 default 两种字段名）
        Object defaultValue = config.get("defaultValue");
        if (defaultValue == null) {
            defaultValue = config.get("default");
        }
        String defaultResult = defaultValue != null ? String.valueOf(defaultValue) : "";
        return padResult(defaultResult, length, padChar);
    }

    /**
     * 填充结果字符串
     */
    private String padResult(String result, Integer length, char padChar) {
        if (length != null && length > 0 && result.length() < length) {
            StringBuilder sb = new StringBuilder();
            for (int i = result.length(); i < length; i++) {
                sb.append(padChar);
            }
            sb.append(result);
            return sb.toString();
        }
        return result;
    }

    @Override
    public String getType() {
        return "conditional";
    }
}

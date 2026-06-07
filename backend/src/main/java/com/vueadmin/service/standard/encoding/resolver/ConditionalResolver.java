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
        // 获取条件字段
        String field = (String) config.get("field");
        if (field == null) {
            return "";
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
                    return String.valueOf(rule.getOrDefault("then", ""));
                }
            }
        }

        // 返回默认值
        Object defaultValue = config.get("default");
        return defaultValue != null ? String.valueOf(defaultValue) : "";
    }

    @Override
    public String getType() {
        return "conditional";
    }
}

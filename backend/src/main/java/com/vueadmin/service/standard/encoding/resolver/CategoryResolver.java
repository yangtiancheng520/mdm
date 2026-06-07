package com.vueadmin.service.standard.encoding.resolver;

import com.vueadmin.service.standard.encoding.EncodingContext;
import com.vueadmin.service.standard.encoding.SegmentResolver;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 分类编码解析器
 * 根据分类字段的值映射到对应的编码
 */
@Component
public class CategoryResolver implements SegmentResolver {

    @Override
    @SuppressWarnings("unchecked")
    public String resolve(Map<String, Object> config, EncodingContext context) {
        // 获取分类字段
        String field = (String) config.get("field");
        if (field == null) {
            return "";
        }

        // 获取字段值
        Object fieldValue = context.getFieldValue(field);
        String fieldValueStr = fieldValue != null ? String.valueOf(fieldValue) : "";

        // 获取映射配置
        Object mappingObj = config.get("mapping");
        if (mappingObj instanceof Map) {
            Map<String, Object> mapping = (Map<String, Object>) mappingObj;
            Object mappedValue = mapping.get(fieldValueStr);
            if (mappedValue != null) {
                return String.valueOf(mappedValue);
            }
        }

        // 返回默认值
        Object defaultValue = config.get("default");
        return defaultValue != null ? String.valueOf(defaultValue) : "";
    }

    @Override
    public String getType() {
        return "category";
    }
}

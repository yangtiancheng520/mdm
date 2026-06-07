package com.vueadmin.service.standard.encoding.resolver;

import com.vueadmin.service.standard.encoding.EncodingContext;
import com.vueadmin.service.standard.encoding.SegmentResolver;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 字段引用解析器
 * 从业务数据中引用字段值作为编码段
 */
@Component
public class FieldResolver implements SegmentResolver {

    @Override
    public String resolve(Map<String, Object> config, EncodingContext context) {
        // 获取字段来源
        String source = (String) config.get("source");
        if (source == null) {
            return "";
        }

        // 获取字段值
        Object value = context.getFieldValue(source);
        if (value == null) {
            return "";
        }

        String strValue = String.valueOf(value);

        // 获取长度限制
        Integer length = getConfigValue(config, "length", null);
        if (length != null && strValue.length() > length) {
            strValue = strValue.substring(0, length);
        }

        // 获取填充字符
        String padding = (String) config.get("padding");
        if (padding != null && length != null && strValue.length() < length) {
            String align = (String) config.getOrDefault("align", "left");
            if ("right".equals(align)) {
                strValue = String.format("%" + length + "s", strValue).replace(" ", padding);
            } else {
                strValue = String.format("%-" + length + "s", strValue).replace(" ", padding);
            }
        }

        return strValue;
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
        return (T) value;
    }

    @Override
    public String getType() {
        return "field";
    }
}

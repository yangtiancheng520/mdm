package com.vueadmin.service.standard.encoding.resolver;

import com.vueadmin.service.standard.encoding.EncodingContext;
import com.vueadmin.service.standard.encoding.SegmentResolver;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * 日期解析器
 */
@Component
public class DateResolver implements SegmentResolver {

    @Override
    public String resolve(Map<String, Object> config, EncodingContext context) {
        // 获取日期格式
        String format = getConfigValue(config, "format", "yyyyMMdd");

        // 获取偏移天数
        Integer offsetDays = getConfigValue(config, "offsetDays", 0);

        // 获取日期来源
        String source = getConfigValue(config, "source", "current");

        LocalDate targetDate;
        if ("current".equals(source)) {
            targetDate = LocalDate.now().plusDays(offsetDays);
        } else {
            // 从业务数据中获取日期
            Object dateValue = context.getFieldValue(source);
            if (dateValue instanceof LocalDate) {
                targetDate = (LocalDate) dateValue;
            } else if (dateValue instanceof LocalDateTime) {
                targetDate = ((LocalDateTime) dateValue).toLocalDate();
            } else if (dateValue instanceof String && !((String) dateValue).isEmpty()) {
                try {
                    targetDate = LocalDate.parse((String) dateValue);
                } catch (Exception e) {
                    targetDate = LocalDate.now();
                }
            } else {
                targetDate = LocalDate.now();
            }
        }

        // 格式化日期
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return targetDate.format(formatter);
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
        return "date";
    }
}
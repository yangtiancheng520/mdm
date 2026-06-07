package com.vueadmin.service.standard.encoding.resolver;

import com.vueadmin.service.standard.encoding.EncodingContext;
import com.vueadmin.service.standard.encoding.SegmentResolver;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 固定值解析器
 */
@Component
public class FixedResolver implements SegmentResolver {

    @Override
    public String resolve(Map<String, Object> config, EncodingContext context) {
        Object value = config.get("value");
        return value != null ? String.valueOf(value) : "";
    }

    @Override
    public String getType() {
        return "fixed";
    }
}

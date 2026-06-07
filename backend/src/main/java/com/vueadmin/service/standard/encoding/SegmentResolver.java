package com.vueadmin.service.standard.encoding;

import java.util.Map;

/**
 * 编码规则段解析器接口
 */
public interface SegmentResolver {

    /**
     * 解析规则段
     *
     * @param config  段配置
     * @param context 编码上下文
     * @return 生成的字符串
     */
    String resolve(Map<String, Object> config, EncodingContext context);

    /**
     * 支持的段类型
     *
     * @return 类型编码
     */
    String getType();
}

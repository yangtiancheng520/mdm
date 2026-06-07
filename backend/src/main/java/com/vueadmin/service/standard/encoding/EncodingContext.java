package com.vueadmin.service.standard.encoding;

import lombok.Data;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 编码生成上下文
 */
@Data
public class EncodingContext {

    /**
     * 业务数据（用于字段引用、条件判断）
     */
    private Map<String, Object> businessData;

    /**
     * 已生成的编码段
     */
    private List<String> generatedSegments;

    /**
     * 当前生成的完整编码
     */
    private StringBuilder currentCode;

    /**
     * 规则ID
     */
    private Long ruleId;

    /**
     * 范围键（用于序列号隔离）
     */
    private String scopeKey;

    /**
     * 扩展属性
     */
    private Map<String, Object> attributes;

    public EncodingContext() {
        this.businessData = new HashMap<>();
        this.generatedSegments = new ArrayList<>();
        this.currentCode = new StringBuilder();
        this.attributes = new HashMap<>();
    }

    public EncodingContext(Map<String, Object> businessData) {
        this();
        if (businessData != null) {
            this.businessData = businessData;
        }
    }

    /**
     * 获取业务字段值
     *
     * @param field 字段名
     * @return 字段值
     */
    public Object getFieldValue(String field) {
        if (businessData == null || field == null) {
            return null;
        }
        return businessData.get(field);
    }

    /**
     * 获取业务字段值（字符串）
     *
     * @param field 字段名
     * @return 字段值字符串
     */
    public String getFieldValueAsString(String field) {
        Object value = getFieldValue(field);
        return value != null ? String.valueOf(value) : "";
    }

    /**
     * 添加已生成段
     *
     * @param segment 段内容
     */
    public void addSegment(String segment) {
        if (segment != null) {
            generatedSegments.add(segment);
            currentCode.append(segment);
        }
    }

    /**
     * 获取当前已生成的编码
     *
     * @return 当前编码
     */
    public String getCurrentCode() {
        return currentCode.toString();
    }

    /**
     * 获取上一个生成的段
     *
     * @return 上一个段
     */
    public String getLastSegment() {
        if (generatedSegments.isEmpty()) {
            return null;
        }
        return generatedSegments.get(generatedSegments.size() - 1);
    }

    /**
     * 设置扩展属性
     *
     * @param key   键
     * @param value 值
     */
    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    /**
     * 获取扩展属性
     *
     * @param key 键
     * @return 值
     */
    public Object getAttribute(String key) {
        return attributes.get(key);
    }
}

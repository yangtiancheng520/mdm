package com.vueadmin.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 编码规则DTO
 */
@Data
public class EncodingRuleDto {

    private Long id;

    /**
     * 规则编码
     */
    private String ruleCode;

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * 规则定义
     */
    private RuleDefinition ruleDefinition;

    /**
     * 适用范围类型
     */
    private String scopeType;

    /**
     * 范围配置
     */
    private String scopeConfig;

    /**
     * 状态
     */
    private String status;

    /**
     * 示例
     */
    private String example;

    /**
     * 描述
     */
    private String description;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新人
     */
    private String updatedBy;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 规则定义结构
     */
    @Data
    public static class RuleDefinition {
        /**
         * 规则段列表
         */
        private List<SegmentConfig> segments;

        /**
         * 全局设置
         */
        private Settings settings;
    }

    /**
     * 规则段配置
     */
    @Data
    public static class SegmentConfig {
        /**
         * 段类型
         * fixed: 固定值
         * date: 日期
         * sequence: 序列号
         * conditional: 条件值
         * field: 字段引用
         * category: 分类编码
         * checkDigit: 校验位
         * random: 随机数
         * expression: 表达式
         */
        private String type;

        /**
         * 段名称
         */
        private String name;

        /**
         * 段配置
         */
        private Map<String, Object> config;
    }

    /**
     * 全局设置
     */
    @Data
    public static class Settings {
        /**
         * 重置周期
         */
        private String resetCycle;

        /**
         * 序列号范围字段
         */
        private List<String> scopeFields;

        /**
         * 最大长度
         */
        private Integer maxLength;

        /**
         * 是否允许手工输入
         */
        private Boolean allowManual;
    }
}

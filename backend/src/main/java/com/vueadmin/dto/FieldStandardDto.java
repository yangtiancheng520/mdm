package com.vueadmin.dto;

import lombok.Data;

/**
 * 字段标准DTO
 * 用于前后端数据传输
 */
@Data
public class FieldStandardDto {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 字段编码，唯一标识
     */
    private String fieldCode;

    /**
     * 字段名称
     */
    private String fieldName;

    /**
     * 字段类型
     * string: 字符
     * integer: 整型
     * boolean: 布尔
     * decimal: 浮点型
     * date: 日期
     * datetime: 日期时间
     * time: 时间
     * text: 长文本
     */
    private String fieldType;

    /**
     * 字段长度（适用于string/decimal类型）
     */
    private Integer length;

    /**
     * 小数精度（适用于decimal类型）
     */
    private Integer precision;

    /**
     * 默认值
     */
    private String defaultValue;

    /**
     * 是否关联值域
     * 0: 否
     * 1: 是
     */
    private Integer isEnum;

    /**
     * 值域ID（关联值域表）
     */
    private Long domainId;

    /**
     * 值域编码
     */
    private String domainCode;

    /**
     * 值域名称
     */
    private String domainName;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 分类（旧字段，兼容）
     */
    private String category;

    /**
     * 状态
     */
    private String status;

    /**
     * 版本号
     */
    private Integer version;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 创建时间
     */
    private String createdAt;

    /**
     * 更新人
     */
    private String updatedBy;

    /**
     * 更新时间
     */
    private String updatedAt;

    /**
     * 描述
     */
    private String description;
}

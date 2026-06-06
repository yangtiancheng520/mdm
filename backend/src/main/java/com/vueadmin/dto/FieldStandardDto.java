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
     * 支持类型：string/number/date/datetime/boolean/text/select/multi_select
     */
    private String fieldType;

    /**
     * 字段长度（适用于string类型）
     */
    private Integer length;

    /**
     * 小数精度（适用于number类型）
     */
    private Integer precision;

    /**
     * 默认值
     */
    private String defaultValue;

    /**
     * 是否必填
     */
    private Integer isRequired;

    /**
     * 验证规则（JSON格式）
     */
    private String validationRule;

    /**
     * 引用ID
     */
    private Long referenceId;

    /**
     * 引用来源
     */
    private String referenceSource;

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
     * draft: 草稿
     * published: 已发布
     * archived: 已归档
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

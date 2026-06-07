package com.vueadmin.dto;

import lombok.Data;

/**
 * 表单组件DTO
 */
@Data
public class FormComponentDto {

    private Long id;
    private Long formId;

    // 组件来源
    private Long viewFieldId;
    private String fieldCode;
    private String fieldName;
    private String fieldType;
    private Long domainId;

    // 布局位置
    private Long groupId;
    private Integer rowIndex;
    private Integer colIndex;
    private Integer colSpan;
    private Integer rowSpan;

    // 字段属性
    private Boolean isRequired;
    private Boolean isReadonly;
    private Boolean isHidden;
    private String defaultValue;
    private String placeholder;

    // 校验配置
    private String validationRules;

    // 样式配置
    private Integer labelWidth;
    private String componentWidth;

    private Integer sort;
    private String status;
}

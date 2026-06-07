package com.vueadmin.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 视图字段DTO
 */
@Data
public class ViewFieldDto {

    private Long id;
    private Long entityId;
    private String fieldCode;
    private String fieldName;

    // 字段来源
    private Long fieldStandardId;
    private String fieldStandardCode; // 字段标准编码（用于显示）
    private String fieldStandardName; // 字段标准名称（用于显示）

    // 基础属性
    private String fieldType;
    private Integer length;
    private Integer precisionVal;

    // 值域配置
    private Long domainId;
    private String domainCode;
    private String domainName;

    // 分组与排序
    private Long groupId;
    private String groupName; // 分组名称（用于显示）
    private Integer sort;
    private Integer columnSpan;

    // 字段属性
    private Boolean isRequired;
    private Boolean isReadonly;
    private Boolean isHidden;
    private Boolean isUnique;
    private Boolean isQuery;
    private Boolean isQueryResult;
    private Boolean isSortable;
    private Boolean isInherited;  // 是否继承字段（修订时）

    // 自动编号配置
    private Boolean autoNumber;
    private Long encodingRuleId;
    private String encodingRuleName;

    // 默认值
    private String defaultValue;
    private String defaultValueType;

    // 参照配置
    private String refSource;
    private Long refId;
    private String refName; // 参照名称（用于显示）
    private String refFilter;
    private String refCascadeField;

    // 枚举配置
    private String enumCode;
    private String enumName; // 值域名称（用于显示）

    // 校验配置
    private Integer minLength;
    private Integer maxLength;
    private BigDecimal minValue;
    private BigDecimal maxValue;
    private String regexPattern;
    private String errorMessage;

    // 联动配置
    private String linkConfig;

    // 其他
    private String placeholder;
    private String tooltip;

    private String status;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
}
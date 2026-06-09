package com.vueadmin.dto;

import lombok.Data;

/**
 * 问题项DTO（用于分组显示）
 */
@Data
public class IssueItemDto {

    private Long id;
    private String issueCode;
    private String fieldCode;         // 字段编码
    private String fieldName;         // 字段名称（中文）
    private String fieldValue;        // 当前值
    private String fieldType;         // 字段类型（text/number/date/select等）
    private String domainCode;        // 值域编码（下拉选项用）
    private String issueType;         // 问题类型
    private String issueLevel;        // 问题级别
    private String issueDesc;         // 问题描述
    private String status;
    private String tableName;         // 表名
    private Long recordId;            // 记录ID
    private Long entityId;            // 实体ID
    private String entityType;        // 实体类型 main/sub
}

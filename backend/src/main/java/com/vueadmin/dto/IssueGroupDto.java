package com.vueadmin.dto;

import lombok.Data;
import java.util.List;

/**
 * 按记录分组的问题DTO
 */
@Data
public class IssueGroupDto {

    // 记录信息
    private Long recordId;
    private String recordName;        // 记录显示名称（如：供应商A）
    private String recordCode;        // 记录编码（如：SUP001）
    private String tableName;         // 表名
    private Long viewId;
    private String viewName;
    private String entityType;        // main/sub

    // 记录当前状态
    private String recordStatus;      // 如：PENDING_QC

    // 问题统计
    private Integer totalIssues;      // 总问题数
    private Integer openIssues;       // 未解决问题数
    private Integer resolvedIssues;   // 已解决问题数

    // 问题列表
    private List<IssueItemDto> issues;
}

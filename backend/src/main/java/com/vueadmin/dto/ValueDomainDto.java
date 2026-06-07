package com.vueadmin.dto;

import lombok.Data;
import java.util.List;

/**
 * 值域DTO
 * 用于前后端数据传输
 */
@Data
public class ValueDomainDto {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 值域编码，唯一标识
     */
    private String domainCode;

    /**
     * 值域名称
     */
    private String domainName;

    /**
     * 存储类型
     * CHAR: 定长字符
     * VARCHAR: 变长字符
     * INT: 整数
     * TINYINT: 小整数(0-255)
     * SMALLINT: 短整数
     * BIGINT: 大整数
     */
    private String dataType;

    /**
     * 存储长度（CHAR/VARCHAR类型使用）
     */
    private Integer dataLength;

    /**
     * 选项列表
     */
    private List<DomainOptionDto> options;

    /**
     * 状态
     * active: 启用
     * inactive: 停用
     */
    private String status;

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
    private String createdAt;

    /**
     * 更新人
     */
    private String updatedBy;

    /**
     * 更新时间
     */
    private String updatedAt;
}

package com.vueadmin.entity.standard;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 值域实体
 * 用于定义可复用的值列表，如性别、币种、国家等
 */
@Data
@Entity
@Table(name = "std_value_domain")
public class ValueDomain {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 值域编码，唯一标识
     */
    @Column(name = "domain_code", unique = true, nullable = false, length = 100)
    private String domainCode;

    /**
     * 值域名称
     */
    @Column(name = "domain_name", nullable = false, length = 200)
    private String domainName;

    /**
     * 存储类型（与数据字典一致）
     * string: 字符
     * integer: 整型
     * decimal: 浮点型
     * boolean: 布尔
     */
    @Column(name = "data_type", length = 20)
    private String dataType;

    /**
     * 存储长度（CHAR/VARCHAR类型使用）
     */
    @Column(name = "data_length")
    private Integer dataLength;

    /**
     * 状态
     * 启用: 启用
     * 停用: 停用
     */
    @Column(name = "status", length = 20)
    private String status = "启用";

    /**
     * 描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 创建人
     */
    @Column(name = "created_by", length = 50)
    private String createdBy;

    /**
     * 创建时间
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * 更新人
     */
    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    /**
     * 更新时间
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = "启用";
        if (dataType == null) dataType = "string";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

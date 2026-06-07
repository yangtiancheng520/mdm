package com.vueadmin.entity.standard;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 编码序列号实体
 * 支持多维度独立的序列号管理
 */
@Data
@Entity
@Table(name = "std_encoding_sequence")
public class EncodingSequence {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 规则ID
     */
    @Column(name = "rule_id", nullable = false)
    private Long ruleId;

    /**
     * 范围键
     * 由多个维度字段值组合而成，如: "FINISHED|BJ01"
     */
    @Column(name = "scope_key", nullable = false, length = 200)
    private String scopeKey;

    /**
     * 当前序列值
     */
    @Column(name = "current_value", nullable = false)
    private Long currentValue = 0L;

    /**
     * 最大值
     */
    @Column(name = "max_value")
    private Long maxValue = 999999L;

    /**
     * 重置周期
     * daily: 每日重置
     * weekly: 每周重置
     * monthly: 每月重置
     * yearly: 每年重置
     * never: 从不重置
     */
    @Column(name = "reset_cycle", length = 20)
    private String resetCycle;

    /**
     * 上次重置日期
     */
    @Column(name = "reset_date")
    private LocalDate resetDate;

    /**
     * 更新时间
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        updatedAt = LocalDateTime.now();
        if (currentValue == null) currentValue = 0L;
        if (resetDate == null) resetDate = LocalDate.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

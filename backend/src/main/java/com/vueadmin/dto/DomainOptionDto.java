package com.vueadmin.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 值域选项DTO
 * 用于表示值域中的单个选项
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DomainOptionDto {

    /**
     * 选项值
     */
    private String value;

    /**
     * 选项标签（显示文本）
     */
    private String label;

    /**
     * 排序号
     */
    private Integer sort;

    /**
     * 是否禁用
     */
    private Boolean disabled;

    /**
     * 扩展属性（JSON格式）
     */
    private String extra;

    /**
     * 简单构造方法
     */
    public DomainOptionDto(String value, String label, Integer sort) {
        this.value = value;
        this.label = label;
        this.sort = sort;
        this.disabled = false;
    }
}

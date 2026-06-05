package com.vueadmin.dto;

import lombok.Data;
import java.util.List;

/**
 * 分页结果封装
 */
@Data
public class PageResult<T> {

    /**
     * 数据列表
     */
    private List<T> list;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页码
     */
    private Integer page;

    /**
     * 每页大小
     */
    private Integer pageSize;

    /**
     * 总页数
     */
    private Integer totalPages;

    public PageResult() {
    }

    public PageResult(List<T> list, Long total) {
        this.list = list;
        this.total = total;
    }

    public PageResult(List<T> list, Long total, Integer page, Integer pageSize) {
        this.list = list;
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
        this.totalPages = pageSize != null && pageSize > 0
                ? (int) Math.ceil((double) total / pageSize)
                : 0;
    }

    /**
     * 创建分页结果
     */
    public static <T> PageResult<T> of(List<T> list, Long total) {
        return new PageResult<>(list, total);
    }

    /**
     * 创建分页结果（带分页信息）
     */
    public static <T> PageResult<T> of(List<T> list, Long total, Integer page, Integer pageSize) {
        return new PageResult<>(list, total, page, pageSize);
    }
}

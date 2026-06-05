package com.vueadmin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 分页请求参数
 */
@Data
public class PageRequest {

    /**
     * 当前页码，从1开始
     */
    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "页码必须大于0")
    private Integer page = 1;

    /**
     * 每页大小
     */
    @NotNull(message = "每页大小不能为空")
    @Min(value = 1, message = "每页大小必须大于0")
    private Integer pageSize = 10;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序方向: asc/desc
     */
    private String sortOrder = "desc";

    /**
     * 获取偏移量
     */
    public int getOffset() {
        return (page - 1) * pageSize;
    }

    /**
     * 获取限制数量
     */
    public int getLimit() {
        return pageSize;
    }
}

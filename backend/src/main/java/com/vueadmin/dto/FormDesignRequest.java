package com.vueadmin.dto;

import lombok.Data;
import java.util.List;

/**
 * 表单设计请求DTO
 */
@Data
public class FormDesignRequest {

    private Long id;
    private String formCode;
    private String formName;
    private String formType;
    private Long viewId;

    // 设计模式
    private String designMode; // auto/blank
    private String styleTemplate; // single/dual/triple/master-detail/grouped/tabs

    // 布局配置
    private String layoutConfig;

    // 功能开关
    private Boolean enableCopy;
    private Boolean enableImport;
    private Boolean enableExport;

    private String description;

    // 分组列表
    private List<FormGroupDto> groups;

    // 组件列表
    private List<FormComponentDto> components;
}

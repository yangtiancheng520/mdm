package com.vueadmin.entity.data;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 数据分类实体
 */
@Data
@Entity
@Table(name = "data_category")
public class DataCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 父级ID，null为顶级
     */
    @Column(name = "parent_id")
    private Long parentId;

    /**
     * 名称
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * 类型：folder(文件夹) / form(表单)
     */
    @Column(name = "type", nullable = false, length = 20)
    private String type;

    /**
     * 关联表单ID（type=form时有效）
     */
    @Column(name = "form_id")
    private Long formId;

    /**
     * 图标
     */
    @Column(name = "icon", length = 50)
    private String icon;

    /**
     * 排序
     */
    @Column(name = "sort")
    private Integer sort;

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
}

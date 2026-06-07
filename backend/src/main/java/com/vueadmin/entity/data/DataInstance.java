package com.vueadmin.entity.data;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 数据实例实体
 */
@Data
@Entity
@Table(name = "data_instance")
public class DataInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 分类ID
     */
    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    /**
     * 表单ID
     */
    @Column(name = "form_id", nullable = false)
    private Long formId;

    /**
     * 视图ID
     */
    @Column(name = "view_id")
    private Long viewId;

    /**
     * 表单数据JSON
     */
    @Column(name = "data_json", columnDefinition = "TEXT")
    private String dataJson;

    /**
     * 状态：active(生效) / obsolete(作废)
     */
    @Column(name = "status", length = 20)
    private String status;

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

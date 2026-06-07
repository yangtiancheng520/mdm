package com.vueadmin.entity.form;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 表单分组实体
 */
@Data
@Entity
@Table(name = "frm_group")
public class FormGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "form_id", nullable = false)
    private Long formId;

    @Column(name = "group_code", nullable = false, length = 100)
    private String groupCode;

    @Column(name = "group_name", nullable = false, length = 200)
    private String groupName;

    @Column(name = "group_type", length = 20)
    private String groupType = "group"; // group/tab/table

    @Column(name = "sort")
    private Integer sort = 0;

    @Column(name = "is_collapsed")
    private Boolean isCollapsed = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

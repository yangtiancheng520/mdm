package com.vueadmin.entity.system;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

/**
 * 组织机构实体
 */
@Data
@Entity
@Table(name = "sys_organization")
@Comment("组织机构表")
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("主键ID")
    private Long id;

    @Column(name = "org_code", nullable = false, unique = true, length = 100)
    @Comment("组织编码")
    private String orgCode;

    @Column(name = "org_name", nullable = false, length = 200)
    @Comment("组织名称")
    private String orgName;

    @Column(name = "org_type", length = 50)
    @Comment("组织类型: company/department/group/position")
    private String orgType;

    @Column(name = "parent_id")
    @Comment("父级ID")
    private Long parentId;

    @Column(name = "level")
    @Comment("层级")
    private Integer level;

    @Column(name = "path", length = 500)
    @Comment("路径")
    private String path;

    @Column(name = "manager", length = 50)
    @Comment("负责人")
    private String manager;

    @Column(name = "phone", length = 20)
    @Comment("电话")
    private String phone;

    @Column(name = "email", length = 100)
    @Comment("邮箱")
    private String email;

    @Column(name = "sort")
    @Comment("排序")
    private Integer sort;

    @Column(name = "status", length = 20)
    @Comment("状态: active/inactive")
    private String status;

    @Column(name = "created_by", length = 50)
    @Comment("创建人")
    private String createdBy;

    @Column(name = "created_at")
    @Comment("创建时间")
    private LocalDateTime createdAt;

    @Column(name = "updated_by", length = 50)
    @Comment("更新人")
    private String updatedBy;

    @Column(name = "updated_at")
    @Comment("更新时间")
    private LocalDateTime updatedAt;

    @Column(name = "description", columnDefinition = "TEXT")
    @Comment("描述")
    private String description;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = "active";
        }
        if (sort == null) {
            sort = 0;
        }
        if (level == null) {
            level = 1;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

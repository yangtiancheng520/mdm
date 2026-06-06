package com.vueadmin.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "bas_permission")
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(unique = true, nullable = false, length = 50)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Type type = Type.menu;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(length = 100)
    private String path;

    @Column(length = 50)
    private String icon;

    @Column(columnDefinition = "INT DEFAULT 0")
    private Integer sort = 0;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Status status = Status.active;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum Type {
        menu, button
    }

    public enum Status {
        active, inactive
    }

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

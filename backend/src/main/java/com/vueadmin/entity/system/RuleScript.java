package com.vueadmin.entity.system;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 规则脚本实体
 */
@Data
@Entity
@Table(name = "sys_rule_script")
public class RuleScript {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "script_code", unique = true, nullable = false, length = 100)
    private String scriptCode;

    @Column(name = "script_name", nullable = false, length = 200)
    private String scriptName;

    @Column(name = "script_type", length = 50)
    private String scriptType;

    @Column(name = "script_content", columnDefinition = "TEXT")
    private String scriptContent;

    @Column(name = "input_params", columnDefinition = "TEXT")
    private String inputParams;

    @Column(name = "output_params", columnDefinition = "TEXT")
    private String outputParams;

    @Column(length = 20)
    private String status = "active";

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(columnDefinition = "TEXT")
    private String description;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = "active";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

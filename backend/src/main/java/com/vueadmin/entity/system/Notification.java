package com.vueadmin.entity.system;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 消息通知实体
 */
@Data
@Entity
@Table(name = "sys_notification")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "notification_code", unique = true, nullable = false, length = 100)
    private String notificationCode;

    @Column(name = "notification_type", nullable = false, length = 50)
    private String notificationType;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(length = 50)
    private String sender;

    @Column(nullable = false, length = 50)
    private String receiver;

    @Column(name = "is_read")
    private Boolean isRead = false;

    @Column(name = "read_time")
    private LocalDateTime readTime;

    @Column(name = "link_url", length = 500)
    private String linkUrl;

    @Column(name = "link_params", columnDefinition = "TEXT")
    private String linkParams;

    private Integer priority = 0;

    @Column(length = 20)
    private String status = "sent";

    @Column(name = "expire_time")
    private LocalDateTime expireTime;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (isRead == null) isRead = false;
        if (priority == null) priority = 0;
        if (status == null) status = "sent";
    }
}

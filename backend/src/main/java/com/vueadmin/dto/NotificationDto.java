package com.vueadmin.dto;

import lombok.Data;
import java.util.Map;

/**
 * 消息通知DTO
 */
@Data
public class NotificationDto {

    private Long id;

    private String notificationCode;

    private String notificationType;

    private String title;

    private String content;

    private String sender;

    private String receiver;

    private Boolean isRead;

    private String readTime;

    private String linkUrl;

    private Map<String, Object> linkParams;

    private Integer priority;

    private String status;

    private String expireTime;

    private String createdAt;

    /**
     * 通知类型显示名称
     */
    private String notificationTypeName;

    /**
     * 优先级显示名称
     */
    private String priorityName;
}

package com.vueadmin.service.system;

import com.vueadmin.dto.NotificationDto;
import com.vueadmin.dto.PageResult;
import com.vueadmin.entity.system.Notification;
import com.vueadmin.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 消息通知服务
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // 通知类型映射
    private static final Map<String, String> NOTIFICATION_TYPE_MAP = new HashMap<>() {{
        put("system", "系统通知");
        put("task", "任务通知");
        put("quality", "质量检测");
        put("distribution", "数据分发");
        put("approval", "审批通知");
    }};

    // 优先级映射
    private static final Map<Integer, String> PRIORITY_MAP = new HashMap<>() {{
        put(0, "普通");
        put(1, "重要");
        put(2, "紧急");
    }};

    /**
     * 获取用户的消息列表
     */
    public PageResult<NotificationDto> getUserNotifications(String receiver, String notificationType, Boolean isRead) {
        List<Notification> notifications = notificationRepository.searchNotifications(receiver, notificationType, isRead);
        List<NotificationDto> list = notifications.stream().map(this::toDto).collect(Collectors.toList());
        return new PageResult<>(list, (long) list.size());
    }

    /**
     * 获取未读消息数量
     */
    public Long getUnreadCount(String receiver) {
        return notificationRepository.countUnreadByReceiver(receiver);
    }

    /**
     * 获取消息详情
     */
    public NotificationDto getById(Long id) {
        return notificationRepository.findById(id)
                .map(this::toDto)
                .orElse(null);
    }

    /**
     * 发送消息通知
     */
    @Transactional
    public Notification send(NotificationDto dto) {
        Notification notification = new Notification();
        notification.setNotificationCode(generateNotificationCode());
        notification.setNotificationType(dto.getNotificationType());
        notification.setTitle(dto.getTitle());
        notification.setContent(dto.getContent());
        notification.setSender(dto.getSender());
        notification.setReceiver(dto.getReceiver());
        notification.setLinkUrl(dto.getLinkUrl());
        notification.setLinkParams(dto.getLinkParams() != null ?
                com.alibaba.fastjson2.JSON.toJSONString(dto.getLinkParams()) : null);
        notification.setPriority(dto.getPriority() != null ? dto.getPriority() : 0);
        notification.setExpireTime(dto.getExpireTime() != null ?
                LocalDateTime.parse(dto.getExpireTime(), formatter) : null);

        return notificationRepository.save(notification);
    }

    /**
     * 发送系统通知给所有用户
     */
    @Transactional
    public void sendToAll(String title, String content, String sender) {
        // 这里简化处理，实际应该查询所有用户并发送
        // 可以考虑使用消息队列异步处理
        Notification notification = new Notification();
        notification.setNotificationCode(generateNotificationCode());
        notification.setNotificationType("system");
        notification.setTitle(title);
        notification.setContent(content);
        notification.setSender(sender);
        notification.setReceiver("all");
        notification.setPriority(1);

        notificationRepository.save(notification);
    }

    /**
     * 标记消息为已读
     */
    @Transactional
    public void markAsRead(Long id) {
        notificationRepository.findById(id).ifPresent(notification -> {
            notification.setIsRead(true);
            notification.setReadTime(LocalDateTime.now());
            notification.setStatus("read");
            notificationRepository.save(notification);
        });
    }

    /**
     * 标记所有消息为已读
     */
    @Transactional
    public int markAllAsRead(String receiver) {
        return notificationRepository.markAllAsRead(receiver, LocalDateTime.now());
    }

    /**
     * 删除消息
     */
    @Transactional
    public void delete(Long id) {
        notificationRepository.deleteById(id);
    }

    /**
     * 清理过期消息
     */
    @Transactional
    public int cleanExpiredNotifications() {
        return notificationRepository.deleteExpiredNotifications(LocalDateTime.now());
    }

    /**
     * 生成消息编码
     */
    private String generateNotificationCode() {
        return "NTF" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    /**
     * 转换为DTO
     */
    private NotificationDto toDto(Notification notification) {
        NotificationDto dto = new NotificationDto();
        dto.setId(notification.getId());
        dto.setNotificationCode(notification.getNotificationCode());
        dto.setNotificationType(notification.getNotificationType());
        dto.setTitle(notification.getTitle());
        dto.setContent(notification.getContent());
        dto.setSender(notification.getSender());
        dto.setReceiver(notification.getReceiver());
        dto.setIsRead(notification.getIsRead());
        dto.setPriority(notification.getPriority());
        dto.setStatus(notification.getStatus());

        // 设置显示名称
        dto.setNotificationTypeName(NOTIFICATION_TYPE_MAP.getOrDefault(
                notification.getNotificationType(), notification.getNotificationType()));
        dto.setPriorityName(PRIORITY_MAP.getOrDefault(notification.getPriority(), "普通"));

        // 解析链接参数
        if (notification.getLinkParams() != null && !notification.getLinkParams().isEmpty()) {
            dto.setLinkParams(com.alibaba.fastjson2.JSON.parseObject(
                    notification.getLinkParams(), Map.class));
        }

        // 格式化时间
        if (notification.getReadTime() != null) {
            dto.setReadTime(notification.getReadTime().format(formatter));
        }
        if (notification.getExpireTime() != null) {
            dto.setExpireTime(notification.getExpireTime().format(formatter));
        }
        if (notification.getCreatedAt() != null) {
            dto.setCreatedAt(notification.getCreatedAt().format(formatter));
        }

        return dto;
    }
}

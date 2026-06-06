package com.vueadmin.controller;

import com.vueadmin.dto.*;
import com.vueadmin.service.system.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 消息通知控制器
 */
@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/list")
    public ApiResponse<PageResult<NotificationDto>> list(
            @RequestParam String receiver,
            @RequestParam(required = false) String notificationType,
            @RequestParam(required = false) Boolean isRead) {
        return ApiResponse.success(notificationService.getUserNotifications(receiver, notificationType, isRead));
    }

    @GetMapping("/unread-count")
    public ApiResponse<Long> getUnreadCount(@RequestParam String receiver) {
        return ApiResponse.success(notificationService.getUnreadCount(receiver));
    }

    @GetMapping("/{id}")
    public ApiResponse<NotificationDto> getById(@PathVariable Long id) {
        return ApiResponse.success(notificationService.getById(id));
    }

    @PostMapping("/send")
    public ApiResponse<NotificationDto> send(@RequestBody NotificationDto dto) {
        com.vueadmin.entity.system.Notification notification = notificationService.send(dto);
        NotificationDto result = new NotificationDto();
        result.setId(notification.getId());
        result.setNotificationCode(notification.getNotificationCode());
        return ApiResponse.success(result);
    }

    @PostMapping("/send-all")
    public ApiResponse<?> sendToAll(@RequestBody Map<String, String> params) {
        String title = params.get("title");
        String content = params.get("content");
        String sender = params.get("sender");
        notificationService.sendToAll(title, content, sender);
        return ApiResponse.success();
    }

    @PostMapping("/read/{id}")
    public ApiResponse<?> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ApiResponse.success();
    }

    @PostMapping("/read-all")
    public ApiResponse<?> markAllAsRead(@RequestParam String receiver) {
        int count = notificationService.markAllAsRead(receiver);
        return ApiResponse.success("已标记" + count + "条消息为已读");
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<?> delete(@PathVariable Long id) {
        notificationService.delete(id);
        return ApiResponse.success();
    }

    @PostMapping("/clean-expired")
    public ApiResponse<?> cleanExpired() {
        int count = notificationService.cleanExpiredNotifications();
        return ApiResponse.success("已清理" + count + "条过期消息");
    }
}

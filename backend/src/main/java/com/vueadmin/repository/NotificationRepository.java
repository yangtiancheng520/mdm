package com.vueadmin.repository;

import com.vueadmin.entity.system.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息通知Repository
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByReceiverOrderByCreatedAtDesc(String receiver);

    List<Notification> findByReceiverAndIsReadOrderByCreatedAtDesc(String receiver, Boolean isRead);

    @Query("SELECT n FROM Notification n WHERE n.receiver = :receiver AND " +
           "(:notificationType IS NULL OR n.notificationType = :notificationType) AND " +
           "(:isRead IS NULL OR n.isRead = :isRead) " +
           "ORDER BY n.createdAt DESC")
    List<Notification> searchNotifications(@Param("receiver") String receiver,
                                           @Param("notificationType") String notificationType,
                                           @Param("isRead") Boolean isRead);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.receiver = :receiver AND n.isRead = false")
    Long countUnreadByReceiver(@Param("receiver") String receiver);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readTime = :readTime, n.status = 'read' WHERE n.receiver = :receiver AND n.isRead = false")
    int markAllAsRead(@Param("receiver") String receiver, @Param("readTime") LocalDateTime readTime);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.expireTime < :expireTime")
    int deleteExpiredNotifications(@Param("expireTime") LocalDateTime expireTime);
}

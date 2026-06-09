package com.vueadmin.repository;

import com.vueadmin.entity.distribution.DisSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DisSubscriptionRepository extends JpaRepository<DisSubscription, Long> {

    List<DisSubscription> findByTopicId(Long topicId);

    List<DisSubscription> findByTopicIdAndStatus(Long topicId, String status);

    List<DisSubscription> findBySystemConfigId(Long systemConfigId);

    @Query("SELECT s FROM DisSubscription s WHERE s.topicId = :topicId ORDER BY s.priority ASC")
    List<DisSubscription> findByTopicIdOrderByPriority(@Param("topicId") Long topicId);

    @Query("SELECT COUNT(s) FROM DisSubscription s WHERE s.topicId = :topicId AND s.status = 'active'")
    int countActiveByTopicId(@Param("topicId") Long topicId);

    void deleteByTopicId(Long topicId);
}

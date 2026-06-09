package com.vueadmin.service.distribution;

import com.vueadmin.dto.ApiResponse;
import com.vueadmin.entity.distribution.DisSubscription;
import com.vueadmin.entity.distribution.DisTopic;
import com.vueadmin.entity.distribution.DisSystemConfig;
import com.vueadmin.exception.BusinessException;
import com.vueadmin.repository.DisSubscriptionRepository;
import com.vueadmin.repository.DisTopicRepository;
import com.vueadmin.repository.DisSystemConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 分发主题服务
 */
@Service
public class DisTopicService {

    @Autowired
    private DisTopicRepository topicRepository;

    @Autowired
    private DisSubscriptionRepository subscriptionRepository;

    @Autowired
    private DisSystemConfigRepository systemConfigRepository;

    /**
     * 获取主题列表
     */
    public Page<DisTopic> getTopicList(String dataType, String status, String keyword, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return topicRepository.search(dataType, status, keyword, pageRequest);
    }

    /**
     * 获取主题详情
     */
    public DisTopic getTopic(Long id) {
        return topicRepository.findById(id)
                .orElseThrow(() -> new BusinessException("主题不存在"));
    }

    /**
     * 创建主题
     */
    @Transactional
    public DisTopic createTopic(DisTopic topic) {
        if (topicRepository.existsByTopicCode(topic.getTopicCode())) {
            throw new BusinessException("主题编码已存在");
        }
        return topicRepository.save(topic);
    }

    /**
     * 更新主题
     */
    @Transactional
    public DisTopic updateTopic(Long id, DisTopic topic) {
        DisTopic existing = topicRepository.findById(id)
                .orElseThrow(() -> new BusinessException("主题不存在"));

        // 检查编码是否重复
        if (!existing.getTopicCode().equals(topic.getTopicCode()) &&
            topicRepository.existsByTopicCode(topic.getTopicCode())) {
            throw new BusinessException("主题编码已存在");
        }

        topic.setId(id);
        topic.setCreatedAt(existing.getCreatedAt());
        topic.setCreatedBy(existing.getCreatedBy());
        return topicRepository.save(topic);
    }

    /**
     * 删除主题
     */
    @Transactional
    public void deleteTopic(Long id) {
        if (!topicRepository.existsById(id)) {
            throw new BusinessException("主题不存在");
        }
        // 删除关联的订阅
        subscriptionRepository.deleteByTopicId(id);
        topicRepository.deleteById(id);
    }

    /**
     * 切换主题状态
     */
    @Transactional
    public DisTopic toggleStatus(Long id) {
        DisTopic topic = getTopic(id);
        topic.setStatus("active".equals(topic.getStatus()) ? "inactive" : "active");
        return topicRepository.save(topic);
    }

    /**
     * 获取主题的订阅列表
     */
    public List<DisSubscription> getSubscriptions(Long topicId) {
        return subscriptionRepository.findByTopicIdOrderByPriority(topicId);
    }

    /**
     * 添加订阅
     */
    @Transactional
    public DisSubscription addSubscription(Long topicId, DisSubscription subscription) {
        DisTopic topic = getTopic(topicId);
        DisSystemConfig systemConfig = systemConfigRepository.findById(subscription.getSystemConfigId())
                .orElseThrow(() -> new BusinessException("系统配置不存在"));

        // 检查是否已订阅
        List<DisSubscription> existing = subscriptionRepository.findByTopicId(topicId);
        boolean exists = existing.stream()
                .anyMatch(s -> s.getSystemConfigId().equals(subscription.getSystemConfigId()));
        if (exists) {
            throw new BusinessException("该系统已订阅此主题");
        }

        subscription.setTopicId(topicId);
        subscription.setTopicName(topic.getTopicName());
        subscription.setSystemConfigName(systemConfig.getConfigName());
        subscription.setSystemType(systemConfig.getSystemType());

        return subscriptionRepository.save(subscription);
    }

    /**
     * 更新订阅
     */
    @Transactional
    public DisSubscription updateSubscription(Long id, DisSubscription subscription) {
        DisSubscription existing = subscriptionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("订阅不存在"));

        subscription.setId(id);
        subscription.setTopicId(existing.getTopicId());
        subscription.setTopicName(existing.getTopicName());
        subscription.setCreatedAt(existing.getCreatedAt());
        subscription.setCreatedBy(existing.getCreatedBy());

        // 更新系统配置信息
        if (!existing.getSystemConfigId().equals(subscription.getSystemConfigId())) {
            DisSystemConfig systemConfig = systemConfigRepository.findById(subscription.getSystemConfigId())
                    .orElseThrow(() -> new BusinessException("系统配置不存在"));
            subscription.setSystemConfigName(systemConfig.getConfigName());
            subscription.setSystemType(systemConfig.getSystemType());
        }

        return subscriptionRepository.save(subscription);
    }

    /**
     * 删除订阅
     */
    @Transactional
    public void deleteSubscription(Long id) {
        subscriptionRepository.deleteById(id);
    }

    /**
     * 获取启用的主题列表（按数据类型）
     */
    public List<DisTopic> getActiveTopicsByDataType(String dataType) {
        return topicRepository.findByDataType(dataType).stream()
                .filter(t -> "active".equals(t.getStatus()))
                .toList();
    }

    /**
     * 获取主题统计
     */
    public TopicStats getStats() {
        TopicStats stats = new TopicStats();
        stats.setTotalTopics(topicRepository.count());
        stats.setActiveTopics(topicRepository.countByStatus("active"));
        return stats;
    }

    public static class TopicStats {
        private long totalTopics;
        private long activeTopics;

        public long getTotalTopics() { return totalTopics; }
        public void setTotalTopics(long totalTopics) { this.totalTopics = totalTopics; }
        public long getActiveTopics() { return activeTopics; }
        public void setActiveTopics(long activeTopics) { this.activeTopics = activeTopics; }
    }
}

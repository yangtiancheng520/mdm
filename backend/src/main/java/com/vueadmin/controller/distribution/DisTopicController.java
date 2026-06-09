package com.vueadmin.controller.distribution;

import com.vueadmin.dto.ApiResponse;
import com.vueadmin.entity.distribution.DisSubscription;
import com.vueadmin.entity.distribution.DisTopic;
import com.vueadmin.service.distribution.DisTopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 分发主题Controller
 */
@RestController
@RequestMapping("/api/distribution/topic")
public class DisTopicController {

    @Autowired
    private DisTopicService topicService;

    // ==================== 主题管理 ====================

    @GetMapping("/list")
    public ApiResponse<Page<DisTopic>> getTopicList(
            @RequestParam(required = false) String dataType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<DisTopic> topics = topicService.getTopicList(dataType, status, keyword, page, size);
        return ApiResponse.success(topics);
    }

    @GetMapping("/{id}")
    public ApiResponse<DisTopic> getTopic(@PathVariable Long id) {
        DisTopic topic = topicService.getTopic(id);
        return ApiResponse.success(topic);
    }

    @PostMapping
    public ApiResponse<DisTopic> createTopic(@RequestBody DisTopic topic) {
        DisTopic saved = topicService.createTopic(topic);
        return ApiResponse.success(saved);
    }

    @PutMapping("/{id}")
    public ApiResponse<DisTopic> updateTopic(@PathVariable Long id, @RequestBody DisTopic topic) {
        DisTopic saved = topicService.updateTopic(id, topic);
        return ApiResponse.success(saved);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteTopic(@PathVariable Long id) {
        topicService.deleteTopic(id);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/toggle")
    public ApiResponse<DisTopic> toggleStatus(@PathVariable Long id) {
        DisTopic topic = topicService.toggleStatus(id);
        return ApiResponse.success(topic);
    }

    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> getStats() {
        DisTopicService.TopicStats stats = topicService.getStats();
        Map<String, Object> result = new HashMap<>();
        result.put("totalTopics", stats.getTotalTopics());
        result.put("activeTopics", stats.getActiveTopics());
        return ApiResponse.success(result);
    }

    // ==================== 订阅管理 ====================

    @GetMapping("/{topicId}/subscriptions")
    public ApiResponse<List<DisSubscription>> getSubscriptions(@PathVariable Long topicId) {
        List<DisSubscription> subscriptions = topicService.getSubscriptions(topicId);
        return ApiResponse.success(subscriptions);
    }

    @PostMapping("/{topicId}/subscription")
    public ApiResponse<DisSubscription> addSubscription(
            @PathVariable Long topicId,
            @RequestBody DisSubscription subscription) {
        DisSubscription saved = topicService.addSubscription(topicId, subscription);
        return ApiResponse.success(saved);
    }

    @PutMapping("/subscription/{id}")
    public ApiResponse<DisSubscription> updateSubscription(
            @PathVariable Long id,
            @RequestBody DisSubscription subscription) {
        DisSubscription saved = topicService.updateSubscription(id, subscription);
        return ApiResponse.success(saved);
    }

    @DeleteMapping("/subscription/{id}")
    public ApiResponse<Void> deleteSubscription(@PathVariable Long id) {
        topicService.deleteSubscription(id);
        return ApiResponse.success();
    }

    // ==================== 数据类型选项 ====================

    @GetMapping("/data-types")
    public ApiResponse<List<Map<String, String>>> getDataTypes() {
        List<Map<String, String>> types = List.of(
                Map.of("value", "VENDOR", "label", "供应商"),
                Map.of("value", "MATERIAL", "label", "物料"),
                Map.of("value", "CUSTOMER", "label", "客户"),
                Map.of("value", "EMPLOYEE", "label", "员工"),
                Map.of("value", "ORGANIZATION", "label", "组织")
        );
        return ApiResponse.success(types);
    }
}

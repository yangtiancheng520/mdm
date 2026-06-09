package com.vueadmin.controller.distribution;

import com.vueadmin.dto.ApiResponse;
import com.vueadmin.entity.distribution.FieldMapping;
import com.vueadmin.entity.distribution.LogDistribution;
import com.vueadmin.repository.FieldMappingRepository;
import com.vueadmin.repository.LogDistributionRepository;
import com.vueadmin.service.distribution.LineageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 血缘数据修复控制器
 */
@RestController
@RequestMapping("/api/distribution/lineage")
public class LineageRepairController {

    private static final Logger logger = LoggerFactory.getLogger(LineageRepairController.class);

    @Autowired
    private LogDistributionRepository logDistributionRepository;

    @Autowired
    private FieldMappingRepository fieldMappingRepository;

    @Autowired
    private LineageService lineageService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 为历史分发日志补充血缘数据
     */
    @PostMapping("/repair")
    public ApiResponse<Map<String, Object>> repairLineageData(
            @RequestParam(required = false) String dataType,
            @RequestParam(required = false, defaultValue = "true") boolean includeFailed) {

        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int failCount = 0;

        try {
            // 查询分发日志但没有血缘数据的
            List<LogDistribution> logs;
            if (dataType != null && !dataType.isEmpty()) {
                logs = logDistributionRepository.findLogsWithoutLineageByType(dataType);
            } else {
                // 包含失败的记录
                logs = logDistributionRepository.findLogsWithoutLineage();
            }

            logger.info("找到 {} 条需要补充血缘的日志", logs.size());

            for (LogDistribution log : logs) {
                try {
                    // 获取字段映射
                    List<FieldMapping> mappings = fieldMappingRepository
                            .findActiveByDataTypeAndSystemConfig(log.getDataType(), log.getSystemConfigId());

                    if (mappings.isEmpty()) {
                        mappings = fieldMappingRepository.findActiveByDataType(log.getDataType());
                    }

                    if (mappings.isEmpty()) {
                        logger.warn("日志 {} 没有找到字段映射配置，跳过", log.getLogCode());
                        failCount++;
                        continue;
                    }

                    // 解析原始数据
                    Map<String, Object> sourceData = parseJson(log.getRequestData());
                    Map<String, Object> mappedData = parseJson(log.getMappedData());

                    if (sourceData.isEmpty()) {
                        logger.warn("日志 {} 的请求数据为空，跳过", log.getLogCode());
                        failCount++;
                        continue;
                    }

                    // 保存血缘数据
                    lineageService.saveFieldLineage(log, sourceData, mappedData, mappings);
                    successCount++;

                } catch (Exception e) {
                    logger.error("处理日志 {} 失败: {}", log.getLogCode(), e.getMessage());
                    failCount++;
                }
            }

            result.put("total", logs.size());
            result.put("successCount", successCount);
            result.put("failCount", failCount);
            result.put("message", String.format("成功补充 %d 条，失败 %d 条", successCount, failCount));

            return ApiResponse.success(result);

        } catch (Exception e) {
            logger.error("修复血缘数据失败", e);
            return ApiResponse.error(500, "修复失败: " + e.getMessage());
        }
    }

    /**
     * 检查血缘数据状态
     */
    @GetMapping("/status")
    public ApiResponse<Map<String, Object>> checkLineageStatus() {
        Map<String, Object> status = new HashMap<>();

        // 统计成功的分发日志数量
        long successLogCount = logDistributionRepository.countByStatus("SUCCESS");

        // 统计有血缘数据的日志数量
        long logsWitLineage = logDistributionRepository.countLogsWithLineage();

        // 统计需要补充血缘的日志数量
        long logsNeedRepair = logDistributionRepository.countSuccessLogsWithoutLineage();

        status.put("successLogCount", successLogCount);
        status.put("logsWitLineage", logsWitLineage);
        status.put("logsNeedRepair", logsNeedRepair);

        return ApiResponse.success(status);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJson(String json) {
        if (json == null || json.isEmpty()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            logger.warn("解析JSON失败: {}", e.getMessage());
            return new HashMap<>();
        }
    }
}

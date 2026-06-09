package com.vueadmin.controller.distribution;

import com.vueadmin.dto.ApiResponse;
import com.vueadmin.dto.distribution.DistributionResultDTO;
import com.vueadmin.entity.distribution.FieldMapping;
import com.vueadmin.entity.distribution.LogDistribution;
import com.vueadmin.entity.distribution.DisSystemConfig;
import com.vueadmin.repository.DisSystemConfigRepository;
import com.vueadmin.repository.FieldMappingRepository;
import com.vueadmin.repository.LogDistributionRepository;
import com.vueadmin.service.distribution.DistributionService;
import com.vueadmin.service.distribution.connector.ConnectorFactory;
import com.vueadmin.service.distribution.connector.SystemConnector;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 分发管理Controller
 */
@RestController
@RequestMapping("/api/distribution")
public class DistributionController {

    @Autowired
    private DistributionService distributionService;

    @Autowired
    private DisSystemConfigRepository systemConfigRepository;

    @Autowired
    private FieldMappingRepository fieldMappingRepository;

    @Autowired
    private LogDistributionRepository logDistributionRepository;

    @Autowired
    private ConnectorFactory connectorFactory;

    @Autowired
    private EntityManager entityManager;

    // 数据类型对应的表名和关键字段
    private static final Map<String, Map<String, String>> DATA_TYPE_CONFIG = Map.of(
        "MATERIAL", Map.of("table", "mdm_material", "codeField", "MATNR", "nameField", "MAKTX"),
        "CUSTOMER", Map.of("table", "mdm_customer", "codeField", "PARTNER_CODE", "nameField", "PARTNER_NAME"),
        "VENDOR", Map.of("table", "mdm_vendor", "codeField", "PARTNER_CODE", "nameField", "PARTNER_NAME")
    );

    // ==================== 系统配置管理 ====================

    /**
     * 获取可分发的数据列表（根据数据类型）
     */
    @GetMapping("/data/list")
    public ApiResponse<Map<String, Object>> getDataList(
            @RequestParam String dataType,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {

        Map<String, String> config = DATA_TYPE_CONFIG.get(dataType);
        if (config == null) {
            return ApiResponse.error(400, "不支持的数据类型: " + dataType);
        }

        String tableName = config.get("table");
        String codeField = config.get("codeField");
        String nameField = config.get("nameField");

        try {
            // 构建查询SQL
            StringBuilder countSql = new StringBuilder("SELECT COUNT(*) FROM " + tableName + " WHERE 1=1");
            StringBuilder dataSql = new StringBuilder("SELECT id, " + codeField + " AS code, " + nameField + " AS name, status, created_at FROM " + tableName + " WHERE 1=1");

            List<Object> params = new java.util.ArrayList<>();

            // 状态过滤（只显示合格数据）
            if (status != null && !status.isEmpty()) {
                countSql.append(" AND status = ?");
                dataSql.append(" AND status = ?");
                params.add(status);
            } else {
                // 默认只显示生效状态的数据
                countSql.append(" AND status IN ('ACTIVE_QUALIFIED', 'ACTIVE_UNQUALIFIED')");
                dataSql.append(" AND status IN ('ACTIVE_QUALIFIED', 'ACTIVE_UNQUALIFIED')");
            }

            // 关键字搜索
            if (keyword != null && !keyword.trim().isEmpty()) {
                countSql.append(" AND (" + codeField + " LIKE ? OR " + nameField + " LIKE ?)");
                dataSql.append(" AND (" + codeField + " LIKE ? OR " + nameField + " LIKE ?)");
                String likeKeyword = "%" + keyword.trim() + "%";
                params.add(likeKeyword);
                params.add(likeKeyword);
            }

            // 查询总数
            Query countQuery = entityManager.createNativeQuery(countSql.toString());
            for (int i = 0; i < params.size(); i++) {
                countQuery.setParameter(i + 1, params.get(i));
            }
            Long total = ((Number) countQuery.getSingleResult()).longValue();

            // 分页查询数据
            dataSql.append(" ORDER BY created_at DESC LIMIT ? OFFSET ?");
            params.add(size);
            params.add((page - 1) * size);

            Query dataQuery = entityManager.createNativeQuery(dataSql.toString());
            for (int i = 0; i < params.size(); i++) {
                dataQuery.setParameter(i + 1, params.get(i));
            }

            @SuppressWarnings("unchecked")
            List<Object[]> results = dataQuery.getResultList();

            // 转换结果
            List<Map<String, Object>> list = new java.util.ArrayList<>();
            for (Object[] row : results) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", row[0]);
                item.put("code", row[1]);
                item.put("name", row[2]);
                item.put("status", row[3]);
                item.put("createdAt", row[4]);
                list.add(item);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("content", list);
            result.put("totalElements", total);
            result.put("totalPages", (total + size - 1) / size);
            result.put("currentPage", page);

            return ApiResponse.success(result);

        } catch (Exception e) {
            // 表不存在时返回空列表
            if (e.getMessage() != null && e.getMessage().contains("doesn't exist")) {
                Map<String, Object> result = new HashMap<>();
                result.put("content", new java.util.ArrayList<>());
                result.put("totalElements", 0L);
                result.put("totalPages", 0);
                result.put("currentPage", page);
                return ApiResponse.success(result);
            }
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取单条数据详情
     */
    @GetMapping("/data/detail")
    public ApiResponse<Map<String, Object>> getDataDetail(
            @RequestParam String dataType,
            @RequestParam Long dataId) {

        Map<String, String> config = DATA_TYPE_CONFIG.get(dataType);
        if (config == null) {
            return ApiResponse.error(400, "不支持的数据类型: " + dataType);
        }

        String tableName = config.get("table");

        try {
            String sql = "SELECT * FROM " + tableName + " WHERE id = ?";
            Query query = entityManager.createNativeQuery(sql);
            query.setParameter(1, dataId);

            @SuppressWarnings("unchecked")
            List<Object[]> results = query.getResultList();

            if (results.isEmpty()) {
                return ApiResponse.error(404, "数据不存在");
            }

            // 获取列名
            Query columnQuery = entityManager.createNativeQuery("SHOW COLUMNS FROM " + tableName);
            @SuppressWarnings("unchecked")
            List<Object[]> columns = columnQuery.getResultList();
            List<String> columnNames = columns.stream()
                    .map(row -> row[0].toString())
                    .toList();

            // 转换为Map
            Map<String, Object> data = new HashMap<>();
            Object[] row = results.get(0);
            for (int i = 0; i < columnNames.size() && i < row.length; i++) {
                data.put(columnNames.get(i), row[i]);
            }

            return ApiResponse.success(data);

        } catch (Exception e) {
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/config/list")
    public ApiResponse<List<DisSystemConfig>> getConfigList() {
        List<DisSystemConfig> configs = systemConfigRepository.findAll();
        return ApiResponse.success(configs);
    }

    @GetMapping("/config/{id}")
    public ApiResponse<DisSystemConfig> getConfig(@PathVariable Long id) {
        return systemConfigRepository.findById(id)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error(404, "配置不存在"));
    }

    @PostMapping("/config")
    public ApiResponse<DisSystemConfig> createConfig(@RequestBody DisSystemConfig config) {
        if (systemConfigRepository.existsByConfigCode(config.getConfigCode())) {
            return ApiResponse.error(400, "配置编码已存在");
        }
        DisSystemConfig saved = systemConfigRepository.save(config);
        return ApiResponse.success(saved);
    }

    @PutMapping("/config/{id}")
    public ApiResponse<DisSystemConfig> updateConfig(@PathVariable Long id, @RequestBody DisSystemConfig config) {
        return systemConfigRepository.findById(id)
                .map(existing -> {
                    config.setId(id);
                    DisSystemConfig saved = systemConfigRepository.save(config);
                    connectorFactory.clearCache(id);
                    return ApiResponse.success(saved);
                })
                .orElse(ApiResponse.error(404, "配置不存在"));
    }

    @DeleteMapping("/config/{id}")
    public ApiResponse<Void> deleteConfig(@PathVariable Long id) {
        if (systemConfigRepository.existsById(id)) {
            systemConfigRepository.deleteById(id);
            connectorFactory.clearCache(id);
            return ApiResponse.success();
        }
        return ApiResponse.error(404, "配置不存在");
    }

    @PostMapping("/config/test/{id}")
    public ApiResponse<Map<String, Object>> testConnection(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();

        try {
            DisSystemConfig config = systemConfigRepository.findById(id).orElse(null);
            if (config == null) {
                result.put("success", false);
                result.put("message", "配置不存在");
                return ApiResponse.success(result);
            }

            SystemConnector connector = connectorFactory.getConnector(config);
            boolean success = connector.testConnection();

            config.setLastTestTime(LocalDateTime.now());
            config.setLastTestResult(success ? "success" : "failed");
            config.setLastTestMsg(success ? "连接成功" : "连接失败");
            systemConfigRepository.save(config);

            result.put("success", success);
            result.put("message", success ? "连接成功" : "连接失败");

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }

        return ApiResponse.success(result);
    }

    // ==================== 字段映射管理 ====================

    @GetMapping("/mapping/list")
    public ApiResponse<List<FieldMapping>> getMappingList(
            @RequestParam(required = false) String dataType,
            @RequestParam(required = false) Long systemConfigId) {

        List<FieldMapping> mappings;
        if (dataType != null && systemConfigId != null) {
            mappings = fieldMappingRepository.findByDataTypeAndSystemConfigId(dataType, systemConfigId);
        } else if (dataType != null) {
            mappings = fieldMappingRepository.findByDataType(dataType);
        } else {
            mappings = fieldMappingRepository.findAll();
        }

        return ApiResponse.success(mappings);
    }

    @GetMapping("/mapping/active")
    public ApiResponse<List<FieldMapping>> getActiveMappings(@RequestParam String dataType) {
        List<FieldMapping> mappings = fieldMappingRepository.findActiveByDataType(dataType);
        return ApiResponse.success(mappings);
    }

    @PostMapping("/mapping/save")
    public ApiResponse<List<FieldMapping>> saveMappings(@RequestBody List<FieldMapping> mappings) {
        List<FieldMapping> saved = fieldMappingRepository.saveAll(mappings);
        return ApiResponse.success(saved);
    }

    @DeleteMapping("/mapping/{id}")
    public ApiResponse<Void> deleteMapping(@PathVariable Long id) {
        fieldMappingRepository.deleteById(id);
        return ApiResponse.success();
    }

    // ==================== 分发执行 ====================

    @PostMapping("/execute")
    public ApiResponse<DistributionResultDTO> distribute(
            @RequestParam String dataType,
            @RequestParam Long dataId,
            @RequestParam Long systemConfigId,
            @RequestBody Map<String, Object> data) {

        DistributionResultDTO result = distributionService.distribute(dataType, dataId, data, systemConfigId);
        return ApiResponse.success(result);
    }

    @PostMapping("/execute/batch")
    public ApiResponse<List<DistributionResultDTO>> batchDistribute(
            @RequestParam String dataType,
            @RequestParam Long systemConfigId,
            @RequestBody List<Map<String, Object>> dataList) {

        List<DistributionResultDTO> results = distributionService.batchDistribute(dataType, dataList, systemConfigId);
        return ApiResponse.success(results);
    }

    @PostMapping("/retry/{logId}")
    public ApiResponse<DistributionResultDTO> retry(@PathVariable Long logId) {
        DistributionResultDTO result = distributionService.retry(logId);
        return ApiResponse.success(result);
    }

    // ==================== 分发日志 ====================

    @GetMapping("/log/list")
    public ApiResponse<Page<LogDistribution>> getLogList(
            @RequestParam(required = false) String dataType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long systemConfigId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        LocalDateTime start = startTime != null ? LocalDateTime.parse(startTime) : null;
        LocalDateTime end = endTime != null ? LocalDateTime.parse(endTime) : null;

        Page<LogDistribution> logs = logDistributionRepository.searchLogs(
                dataType, status, systemConfigId, start, end, pageRequest);

        return ApiResponse.success(logs);
    }

    @GetMapping("/log/{id}")
    public ApiResponse<LogDistribution> getLog(@PathVariable Long id) {
        return logDistributionRepository.findById(id)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error(404, "日志不存在"));
    }

    @GetMapping("/log/history")
    public ApiResponse<List<LogDistribution>> getLogHistory(
            @RequestParam String dataType,
            @RequestParam Long dataId) {

        List<LogDistribution> logs = logDistributionRepository.findByDataTypeAndDataIdOrderByCreatedDesc(dataType, dataId);
        return ApiResponse.success(logs);
    }

    @GetMapping("/log/stats")
    public ApiResponse<Map<String, Object>> getLogStats(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {

        LocalDateTime start = startTime != null ? LocalDateTime.parse(startTime) : LocalDateTime.now().minusDays(7);
        LocalDateTime end = endTime != null ? LocalDateTime.parse(endTime) : LocalDateTime.now();

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", logDistributionRepository.countByCreatedBetween(start, end));

        List<Object[]> statusCounts = logDistributionRepository.countByStatusAndCreatedBetween(start, end);
        Map<String, Long> statusMap = new HashMap<>();
        for (Object[] row : statusCounts) {
            statusMap.put((String) row[0], (Long) row[1]);
        }
        stats.put("byStatus", statusMap);

        return ApiResponse.success(stats);
    }
}

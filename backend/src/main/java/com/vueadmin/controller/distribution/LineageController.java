package com.vueadmin.controller.distribution;

import com.vueadmin.dto.ApiResponse;
import com.vueadmin.dto.distribution.FieldLineageFlatDTO;
import com.vueadmin.dto.distribution.LineageDetailDTO;
import com.vueadmin.dto.distribution.LineageSummaryDTO;
import com.vueadmin.service.distribution.LineageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 血缘分析Controller
 */
@RestController
@RequestMapping("/api/distribution/lineage")
public class LineageController {

    @Autowired
    private LineageService lineageService;

    /**
     * 获取扁平化的字段级血缘列表（直接展示所有字段）
     */
    @GetMapping("/field-list")
    public ApiResponse<Page<FieldLineageFlatDTO>> getFieldLineageFlatList(
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

        Page<FieldLineageFlatDTO> result = lineageService.getFieldLineageFlatList(
                dataType, status, systemConfigId, start, end, pageRequest);

        return ApiResponse.success(result);
    }

    /**
     * 血缘分析搜索
     */
    @GetMapping("/search")
    public ApiResponse<Page<LineageSummaryDTO>> searchLineage(
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

        Page<LineageSummaryDTO> result = lineageService.searchLineage(
                dataType, status, systemConfigId, start, end, pageRequest);

        return ApiResponse.success(result);
    }

    /**
     * 获取字段级血缘详情
     */
    @GetMapping("/field/{logId}")
    public ApiResponse<LineageDetailDTO> getFieldLineage(@PathVariable Long logId) {
        LineageDetailDTO lineage = lineageService.getLineageDetail(logId);
        return ApiResponse.success(lineage);
    }

    /**
     * 获取数据的完整血缘（包含所有分发记录）
     */
    @GetMapping("/data")
    public ApiResponse<List<LineageDetailDTO>> getDataLineage(
            @RequestParam String dataType,
            @RequestParam Long dataId) {
        List<LineageDetailDTO> lineages = lineageService.getDataLineage(dataType, dataId);
        return ApiResponse.success(lineages);
    }

    /**
     * 获取字段级血缘列表
     */
    @GetMapping("/fields/{logId}")
    public ApiResponse<List<LineageDetailDTO.FieldLineageDTO>> getFieldLineageList(@PathVariable Long logId) {
        List<LineageDetailDTO.FieldLineageDTO> fieldLineages = lineageService.getFieldLineage(logId);
        return ApiResponse.success(fieldLineages);
    }
}

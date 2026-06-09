package com.vueadmin.controller.quality;

import com.vueadmin.dto.ApiResponse;
import com.vueadmin.dto.IssueGroupDto;
import com.vueadmin.dto.IssueItemDto;
import com.vueadmin.dto.QualityIssueDto;
import com.vueadmin.service.quality.QualityIssueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 问题管理Controller
 */
@RestController
@RequestMapping("/api/quality/issue")
@RequiredArgsConstructor
@Tag(name = "问题管理", description = "质量问题的查询和处理")
public class QualityIssueController {

    private final QualityIssueService qualityIssueService;

    /**
     * 获取问题列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取问题列表")
    public ApiResponse<List<QualityIssueDto>> getIssueList(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String issueLevel,
            @RequestParam(required = false) Long viewId) {
        List<QualityIssueDto> issues = qualityIssueService.getIssueList(status, issueLevel, viewId);
        return ApiResponse.success(issues);
    }

    /**
     * 获取待处理问题
     */
    @GetMapping("/open")
    @Operation(summary = "获取待处理问题")
    public ApiResponse<List<QualityIssueDto>> getOpenIssues() {
        List<QualityIssueDto> issues = qualityIssueService.getOpenIssues();
        return ApiResponse.success(issues);
    }

    /**
     * 获取问题详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取问题详情")
    public ApiResponse<QualityIssueDto> getIssueById(@PathVariable Long id) {
        QualityIssueDto issue = qualityIssueService.getIssueById(id);
        return ApiResponse.success(issue);
    }

    /**
     * 指派问题
     */
    @PostMapping("/assign/{id}")
    @Operation(summary = "指派问题")
    public ApiResponse<QualityIssueDto> assignIssue(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String assignee = body.get("assignee");
        QualityIssueDto issue = qualityIssueService.assignIssue(id, assignee);
        return ApiResponse.success(issue);
    }

    /**
     * 批量指派
     */
    @PostMapping("/batch-assign")
    @Operation(summary = "批量指派")
    public ApiResponse<Void> batchAssign(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Long> ids = (List<Long>) body.get("ids");
        String assignee = (String) body.get("assignee");
        qualityIssueService.batchAssign(ids, assignee);
        return ApiResponse.success(null);
    }

    /**
     * 解决问题
     */
    @PostMapping("/resolve/{id}")
    @Operation(summary = "解决问题")
    public ApiResponse<QualityIssueDto> resolveIssue(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String resolution = body.get("resolution");
        String currentUser = getCurrentUser();
        QualityIssueDto issue = qualityIssueService.resolveIssue(id, resolution, currentUser);
        return ApiResponse.success(issue);
    }

    /**
     * 忽略问题
     */
    @PostMapping("/ignore/{id}")
    @Operation(summary = "忽略问题")
    public ApiResponse<QualityIssueDto> ignoreIssue(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String reason = body.get("reason");
        String currentUser = getCurrentUser();
        QualityIssueDto issue = qualityIssueService.ignoreIssue(id, reason, currentUser);
        return ApiResponse.success(issue);
    }

    /**
     * 关闭问题
     */
    @PostMapping("/close/{id}")
    @Operation(summary = "关闭问题")
    public ApiResponse<QualityIssueDto> closeIssue(@PathVariable Long id) {
        String currentUser = getCurrentUser();
        QualityIssueDto issue = qualityIssueService.closeIssue(id, currentUser);
        return ApiResponse.success(issue);
    }

    /**
     * 统计问题数量
     */
    @GetMapping("/count")
    @Operation(summary = "统计问题数量")
    public ApiResponse<Long> countByStatus(@RequestParam String status) {
        Long count = qualityIssueService.countByStatus(status);
        return ApiResponse.success(count);
    }

    /**
     * 获取当前用户
     */
    private String getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "system";
    }

    /**
     * 获取按记录分组的问题列表
     */
    @GetMapping("/grouped")
    @Operation(summary = "获取按记录分组的问题列表")
    public ApiResponse<List<IssueGroupDto>> getGroupedIssues(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String issueLevel,
            @RequestParam(required = false) Long viewId) {
        List<IssueGroupDto> groups = qualityIssueService.getGroupedIssues(status, issueLevel, viewId);
        return ApiResponse.success(groups);
    }

    /**
     * 修改字段值并解决问题
     */
    @PostMapping("/update-field/{id}")
    @Operation(summary = "修改字段值并解决问题")
    public ApiResponse<IssueItemDto> updateFieldAndResolve(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String newValue = body.get("newValue");
        String currentUser = getCurrentUser();
        IssueItemDto issue = qualityIssueService.updateFieldAndResolve(id, newValue, currentUser);
        return ApiResponse.success(issue);
    }
}

package com.vueadmin.controller.standard;

import com.vueadmin.dto.ApiResponse;
import com.vueadmin.dto.ViewDefinitionDto;
import com.vueadmin.service.standard.ViewDefinitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 视图定义Controller
 */
@RestController
@RequestMapping("/api/standard/view")
@RequiredArgsConstructor
@Tag(name = "视图定义管理", description = "视图模型的增删改查及发布归档等操作")
public class ViewDefinitionController {

    private final ViewDefinitionService viewDefinitionService;

    /**
     * 获取视图列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取视图列表", description = "支持按关键字、分类、状态筛选")
    public ApiResponse<List<ViewDefinitionDto>> getViewList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String status) {
        List<ViewDefinitionDto> views = viewDefinitionService.getViewList(keyword, categoryId, status);
        return ApiResponse.success(views);
    }

    /**
     * 获取视图详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取视图详情", description = "获取完整的视图结构，包括实体、字段、校验规则")
    public ApiResponse<ViewDefinitionDto> getViewDetail(@PathVariable Long id) {
        ViewDefinitionDto view = viewDefinitionService.getViewDetail(id);
        return ApiResponse.success(view);
    }

    /**
     * 根据视图编码获取已发布版本
     */
    @GetMapping("/published/{viewCode}")
    @Operation(summary = "获取已发布视图", description = "根据视图编码获取已发布的版本")
    public ApiResponse<ViewDefinitionDto> getPublishedView(@PathVariable String viewCode) {
        ViewDefinitionDto view = viewDefinitionService.getPublishedView(viewCode);
        return ApiResponse.success(view);
    }

    /**
     * 创建视图
     */
    @PostMapping
    @Operation(summary = "创建视图", description = "创建新的视图模型")
    public ApiResponse<ViewDefinitionDto> createView(@RequestBody ViewDefinitionDto dto) {
        ViewDefinitionDto view = viewDefinitionService.createView(dto);
        return ApiResponse.success(view);
    }

    /**
     * 更新视图
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新视图", description = "更新视图模型，已发布状态会创建新版本")
    public ApiResponse<ViewDefinitionDto> updateView(@PathVariable Long id, @RequestBody ViewDefinitionDto dto) {
        ViewDefinitionDto view = viewDefinitionService.updateView(id, dto);
        return ApiResponse.success(view);
    }

    /**
     * 提交审批
     */
    @PostMapping("/{id}/submit")
    @Operation(summary = "提交审批", description = "将视图提交审批，需要验证主表有效性")
    public ApiResponse<ViewDefinitionDto> submitForApproval(@PathVariable Long id) {
        ViewDefinitionDto view = viewDefinitionService.submitForApproval(id);
        return ApiResponse.success(view);
    }

    /**
     * 审批视图
     */
    @PostMapping("/{id}/approve")
    @Operation(summary = "审批视图", description = "审批已提交的视图，通过或驳回")
    public ApiResponse<ViewDefinitionDto> approveView(
            @PathVariable Long id,
            @RequestBody Map<String, Object> params) {
        boolean approved = Boolean.TRUE.equals(params.get("approved"));
        String comment = (String) params.get("comment");
        ViewDefinitionDto view = viewDefinitionService.approveView(id, approved, comment);
        return ApiResponse.success(view);
    }

    /**
     * 撤销审批
     */
    @PostMapping("/{id}/cancel")
    @Operation(summary = "撤销审批", description = "撤销已提交的审批申请，退回草稿状态")
    public ApiResponse<ViewDefinitionDto> cancelApproval(@PathVariable Long id) {
        ViewDefinitionDto view = viewDefinitionService.cancelApproval(id);
        return ApiResponse.success(view);
    }

    /**
     * 发布视图
     */
    @PostMapping("/{id}/publish")
    @Operation(summary = "发布视图", description = "将审批通过的视图发布")
    public ApiResponse<ViewDefinitionDto> publishView(@PathVariable Long id) {
        ViewDefinitionDto view = viewDefinitionService.publishView(id);
        return ApiResponse.success(view);
    }

    /**
     * 停用视图
     */
    @PostMapping("/{id}/disable")
    @Operation(summary = "停用视图", description = "停用已发布的视图")
    public ApiResponse<ViewDefinitionDto> disableView(@PathVariable Long id) {
        ViewDefinitionDto view = viewDefinitionService.disableView(id);
        return ApiResponse.success(view);
    }

    /**
     * 启用视图
     */
    @PostMapping("/{id}/enable")
    @Operation(summary = "启用视图", description = "启用已停用的视图")
    public ApiResponse<ViewDefinitionDto> enableView(@PathVariable Long id) {
        ViewDefinitionDto view = viewDefinitionService.enableView(id);
        return ApiResponse.success(view);
    }

    /**
     * 修订视图
     */
    @PostMapping("/{id}/revise")
    @Operation(summary = "修订视图", description = "创建新版本进行修订")
    public ApiResponse<ViewDefinitionDto> reviseView(@PathVariable Long id) {
        ViewDefinitionDto view = viewDefinitionService.reviseView(id);
        return ApiResponse.success(view);
    }

    /**
     * 删除视图
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除视图", description = "删除视图（已发布状态不可删除）")
    public ApiResponse<Void> deleteView(@PathVariable Long id) {
        viewDefinitionService.deleteView(id);
        return ApiResponse.success();
    }

    /**
     * 获取版本历史
     */
    @GetMapping("/versions/{viewCode}")
    @Operation(summary = "获取版本历史", description = "获取某个视图的所有版本")
    public ApiResponse<List<ViewDefinitionDto>> getVersionHistory(@PathVariable String viewCode) {
        List<ViewDefinitionDto> versions = viewDefinitionService.getVersionHistory(viewCode);
        return ApiResponse.success(versions);
    }
}

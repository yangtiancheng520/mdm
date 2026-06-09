package com.vueadmin.controller.standard;

import com.vueadmin.dto.ApiResponse;
import com.vueadmin.dto.DomainOptionDto;
import com.vueadmin.dto.PageResult;
import com.vueadmin.dto.ValueDomainDto;
import com.vueadmin.service.standard.ValueDomainService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 值域控制器
 * 提供值域的REST API接口
 */
@RestController
@RequestMapping("/api/value-domain")
@RequiredArgsConstructor
@Tag(name = "值域管理", description = "值域的增删改查及启用停用等操作")
public class ValueDomainController {

    private final ValueDomainService valueDomainService;

    /**
     * 分页查询值域列表
     *
     * @param domainCode 值域编码（模糊查询）
     * @param domainName 值域名称（模糊查询）
     * @param dataType   数据类型
     * @param status     状态
     * @param page       页码
     * @param pageSize   每页大小
     * @return 分页结果
     */
    @GetMapping("/list")
    @Operation(summary = "分页查询值域列表", description = "支持按值域编码、名称、数据类型、状态筛选")
    public ApiResponse<PageResult<ValueDomainDto>> list(
            @Parameter(description = "值域编码（模糊查询）")
            @RequestParam(required = false) String domainCode,
            @Parameter(description = "值域名称（模糊查询）")
            @RequestParam(required = false) String domainName,
            @Parameter(description = "数据类型")
            @RequestParam(required = false) String dataType,
            @Parameter(description = "状态（active/inactive）")
            @RequestParam(required = false) String status,
            @Parameter(description = "页码，默认1")
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @Parameter(description = "每页大小，默认10")
            @RequestParam(required = false, defaultValue = "10") Integer pageSize) {

        PageResult<ValueDomainDto> result = valueDomainService.search(
                domainCode, domainName, dataType, status, page, pageSize);
        return ApiResponse.success(result);
    }

    /**
     * 获取所有启用的值域列表
     *
     * @return 值域列表
     */
    @GetMapping("/active")
    @Operation(summary = "获取所有启用的值域列表", description = "获取所有启用状态的值域，用于下拉选择")
    public ApiResponse<List<ValueDomainDto>> getAllActive() {
        return ApiResponse.success(valueDomainService.getAllActive());
    }

    /**
     * 获取所有值域列表（不分页）
     *
     * @return 值域列表
     */
    @GetMapping("/all")
    @Operation(summary = "获取所有值域列表", description = "获取所有值域，不分页")
    public ApiResponse<List<ValueDomainDto>> getAll() {
        return ApiResponse.success(valueDomainService.getAll());
    }

    /**
     * 根据ID获取值域详情
     *
     * @param id 主键ID
     * @return 值域详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取值域详情", description = "根据ID获取值域详细信息")
    public ApiResponse<ValueDomainDto> getById(
            @Parameter(description = "值域ID")
            @PathVariable Long id) {
        ValueDomainDto dto = valueDomainService.getById(id);
        if (dto == null) {
            return ApiResponse.error(404, "值域不存在");
        }
        return ApiResponse.success(dto);
    }

    /**
     * 根据值域编码获取值域详情
     *
     * @param domainCode 值域编码
     * @return 值域详情
     */
    @GetMapping("/code/{domainCode}")
    @Operation(summary = "根据值域编码获取详情", description = "根据值域编码获取值域详细信息")
    public ApiResponse<ValueDomainDto> getByDomainCode(
            @Parameter(description = "值域编码")
            @PathVariable String domainCode) {
        ValueDomainDto dto = valueDomainService.getByDomainCode(domainCode);
        if (dto == null) {
            return ApiResponse.error(404, "值域不存在");
        }
        return ApiResponse.success(dto);
    }

    /**
     * 根据值域编码获取选项列表
     * 用于下拉框等场景
     *
     * @param domainCode 值域编码
     * @return 选项列表
     */
    @GetMapping("/code/{domainCode}/options")
    @Operation(summary = "获取值域选项列表", description = "根据值域编码获取选项列表，用于下拉框等场景")
    public ApiResponse<List<DomainOptionDto>> getOptionsByDomainCode(
            @Parameter(description = "值域编码")
            @PathVariable String domainCode) {
        return ApiResponse.success(valueDomainService.getOptionsByDomainCode(domainCode));
    }

    /**
     * 创建值域
     *
     * @param dto 值域DTO
     * @return 创建后的值域
     */
    @PostMapping
    @Operation(summary = "创建值域", description = "创建新的值域")
    public ApiResponse<ValueDomainDto> create(
            @Parameter(description = "值域信息")
            @RequestBody ValueDomainDto dto) {
        try {
            ValueDomainDto result = valueDomainService.create(dto);
            return ApiResponse.success("创建成功", result);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    /**
     * 更新值域
     *
     * @param id  主键ID
     * @param dto 值域DTO
     * @return 更新后的值域
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新值域", description = "更新指定ID的值域信息")
    public ApiResponse<ValueDomainDto> update(
            @Parameter(description = "值域ID")
            @PathVariable Long id,
            @Parameter(description = "值域信息")
            @RequestBody ValueDomainDto dto) {
        try {
            ValueDomainDto result = valueDomainService.update(id, dto);
            return ApiResponse.success("更新成功", result);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    /**
     * 更新值域项
     *
     * @param id      值域ID
     * @param options 选项列表
     * @return 操作结果
     */
    @PutMapping("/{id}/options")
    @Operation(summary = "更新值域项", description = "更新指定值域的选项列表")
    public ApiResponse<?> updateOptions(
            @Parameter(description = "值域ID")
            @PathVariable Long id,
            @Parameter(description = "选项列表")
            @RequestBody List<DomainOptionDto> options) {
        try {
            valueDomainService.updateOptions(id, options);
            return ApiResponse.success("更新成功");
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    /**
     * 删除值域
     *
     * @param id 主键ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除值域", description = "删除指定ID的值域")
    public ApiResponse<?> delete(
            @Parameter(description = "值域ID")
            @PathVariable Long id) {
        try {
            valueDomainService.delete(id);
            return ApiResponse.success("删除成功");
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    /**
     * 发布值域（草稿 -> 启用）
     *
     * @param id 主键ID
     * @return 更新后的值域
     */
    @PutMapping("/{id}/publish")
    @Operation(summary = "发布值域", description = "发布值域（草稿 -> 启用）")
    public ApiResponse<ValueDomainDto> publish(
            @Parameter(description = "值域ID")
            @PathVariable Long id) {
        try {
            ValueDomainDto result = valueDomainService.publish(id);
            return ApiResponse.success("发布成功", result);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    /**
     * 批量删除值域
     *
     * @param params 包含ids的参数
     * @return 操作结果
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除值域", description = "批量删除多个值域")
    public ApiResponse<?> batchDelete(
            @Parameter(description = "批量删除参数，包含ids数组")
            @RequestBody Map<String, Object> params) {
        try {
            @SuppressWarnings("unchecked")
            List<Integer> idInts = (List<Integer>) params.get("ids");

            List<Long> ids = idInts.stream()
                    .map(Integer::longValue)
                    .collect(java.util.stream.Collectors.toList());

            valueDomainService.batchDelete(ids);
            return ApiResponse.success("批量删除成功");
        } catch (Exception e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    /**
     * 启用值域
     *
     * @param id 主键ID
     * @return 更新后的值域
     */
    @PutMapping("/{id}/activate")
    @Operation(summary = "启用值域", description = "将停用状态的值域启用")
    public ApiResponse<ValueDomainDto> activate(
            @Parameter(description = "值域ID")
            @PathVariable Long id) {
        try {
            ValueDomainDto result = valueDomainService.activate(id);
            return ApiResponse.success("启用成功", result);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    /**
     * 停用值域
     *
     * @param id 主键ID
     * @return 更新后的值域
     */
    @PutMapping("/{id}/deactivate")
    @Operation(summary = "停用值域", description = "将启用状态的值域停用")
    public ApiResponse<ValueDomainDto> deactivate(
            @Parameter(description = "值域ID")
            @PathVariable Long id) {
        try {
            ValueDomainDto result = valueDomainService.deactivate(id);
            return ApiResponse.success("停用成功", result);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    /**
     * 获取值域统计信息
     *
     * @return 统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取统计信息", description = "获取各状态的值域数量统计")
    public ApiResponse<Map<String, Long>> getStatistics() {
        Map<String, Long> statistics = new java.util.HashMap<>();
        statistics.put("active", valueDomainService.countByStatus("active"));
        statistics.put("inactive", valueDomainService.countByStatus("inactive"));
        statistics.put("total", valueDomainService.countByStatus("active") + valueDomainService.countByStatus("inactive"));
        return ApiResponse.success(statistics);
    }
}

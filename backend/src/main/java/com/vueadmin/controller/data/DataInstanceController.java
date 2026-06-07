package com.vueadmin.controller.data;

import com.vueadmin.dto.ApiResponse;
import com.vueadmin.dto.DataInstanceDto;
import com.vueadmin.service.data.DataInstanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 数据实例Controller
 */
@RestController
@RequestMapping("/api/data-instance")
@RequiredArgsConstructor
public class DataInstanceController {

    private final DataInstanceService instanceService;

    /**
     * 查询数据列表
     */
    @GetMapping("/list")
    public ApiResponse<List<DataInstanceDto>> getList(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long formId) {
        List<DataInstanceDto> list = instanceService.getList(categoryId, formId);
        return ApiResponse.success(list);
    }

    /**
     * 获取数据详情
     */
    @GetMapping("/{id}")
    public ApiResponse<DataInstanceDto> getById(@PathVariable Long id) {
        DataInstanceDto dto = instanceService.getById(id);
        return ApiResponse.success(dto);
    }

    /**
     * 保存数据
     */
    @PostMapping
    public ApiResponse<DataInstanceDto> save(@RequestBody Map<String, Object> request) {
        Long categoryId = Long.valueOf(request.get("categoryId").toString());
        Long formId = Long.valueOf(request.get("formId").toString());
        Object data = request.get("data");

        DataInstanceDto dto = instanceService.save(categoryId, formId, data);
        return ApiResponse.success(dto);
    }

    /**
     * 更新数据
     */
    @PutMapping("/{id}")
    public ApiResponse<DataInstanceDto> update(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        Object data = request.get("data");

        DataInstanceDto dto = instanceService.update(id, data);
        return ApiResponse.success(dto);
    }

    /**
     * 删除数据
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        instanceService.delete(id);
        return ApiResponse.success();
    }
}
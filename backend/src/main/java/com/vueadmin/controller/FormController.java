package com.vueadmin.controller;

import com.vueadmin.dto.ApiResponse;
import com.vueadmin.dto.FormDesignRequest;
import com.vueadmin.dto.FormDto;
import com.vueadmin.service.form.FormService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/form")
@RequiredArgsConstructor
public class FormController {

    private final FormService formService;

    /**
     * 获取表单列表
     */
    @GetMapping("/list")
    public ApiResponse<List<FormDto>> getFormList(
            @RequestParam(required = false) String formName,
            @RequestParam(required = false) String formType,
            @RequestParam(required = false) Long viewId,
            @RequestParam(required = false) String status) {
        List<FormDto> list = formService.getFormList(formName, formType, viewId, status);
        return ApiResponse.success(list);
    }

    /**
     * 获取表单详情
     */
    @GetMapping("/{id}")
    public ApiResponse<FormDto> getForm(@PathVariable Long id) {
        FormDto form = formService.getFormById(id);
        return ApiResponse.success(form);
    }

    /**
     * 获取表单设计数据
     */
    @GetMapping("/{id}/design")
    public ApiResponse<FormDesignRequest> getFormDesign(@PathVariable Long id) {
        FormDesignRequest design = formService.getFormDesign(id);
        return ApiResponse.success(design);
    }

    /**
     * 创建空白表单
     */
    @PostMapping("/create")
    public ApiResponse<FormDto> createForm(@RequestBody FormDesignRequest request) {
        FormDto form = formService.createForm(request);
        return ApiResponse.success(form);
    }

    /**
     * 自动生成表单
     */
    @PostMapping("/auto-generate")
    public ApiResponse<FormDto> autoGenerateForm(@RequestBody FormDesignRequest request) {
        FormDto form = formService.autoGenerateForm(request);
        return ApiResponse.success(form);
    }

    /**
     * 保存表单设计
     */
    @PostMapping("/{id}/save-design")
    public ApiResponse<Void> saveFormDesign(@PathVariable Long id, @RequestBody FormDesignRequest request) {
        request.setId(id);
        formService.saveFormDesign(request);
        return ApiResponse.success();
    }

    /**
     * 更新表单基本信息
     */
    @PutMapping("/{id}")
    public ApiResponse<FormDto> updateForm(@PathVariable Long id, @RequestBody FormDesignRequest request) {
        FormDto form = formService.updateForm(id, request);
        return ApiResponse.success(form);
    }

    /**
     * 删除表单
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteForm(@PathVariable Long id) {
        formService.deleteForm(id);
        return ApiResponse.success();
    }

    /**
     * 发布表单
     */
    @PostMapping("/{id}/publish")
    public ApiResponse<Void> publishForm(@PathVariable Long id) {
        formService.publishForm(id);
        return ApiResponse.success();
    }

    /**
     * 设置默认表单
     */
    @PostMapping("/{id}/set-default")
    public ApiResponse<Void> setDefaultForm(@PathVariable Long id) {
        formService.setDefaultForm(id);
        return ApiResponse.success();
    }
}
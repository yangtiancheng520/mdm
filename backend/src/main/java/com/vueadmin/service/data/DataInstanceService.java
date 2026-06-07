package com.vueadmin.service.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vueadmin.dto.DataInstanceDto;
import com.vueadmin.entity.data.DataCategory;
import com.vueadmin.entity.data.DataInstance;
import com.vueadmin.entity.form.Form;
import com.vueadmin.exception.BusinessException;
import com.vueadmin.repository.DataCategoryRepository;
import com.vueadmin.repository.DataInstanceRepository;
import com.vueadmin.repository.FormRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据实例Service
 */
@Service
@RequiredArgsConstructor
public class DataInstanceService {

    private final DataInstanceRepository instanceRepository;
    private final DataCategoryRepository categoryRepository;
    private final FormRepository formRepository;
    private final ObjectMapper objectMapper;

    /**
     * 查询数据列表
     */
    public List<DataInstanceDto> getList(Long categoryId, Long formId) {
        List<DataInstance> instances;

        if (categoryId != null && formId != null) {
            instances = instanceRepository.findByCategoryIdAndFormId(categoryId, formId);
        } else if (formId != null) {
            instances = instanceRepository.findByFormId(formId);
        } else if (categoryId != null) {
            instances = instanceRepository.findByCategoryId(categoryId);
        } else {
            instances = instanceRepository.findAll();
        }

        return instances.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 获取数据详情
     */
    public DataInstanceDto getById(Long id) {
        DataInstance instance = instanceRepository.findById(id)
                .orElseThrow(() -> new BusinessException("数据不存在"));
        return convertToDto(instance);
    }

    /**
     * 保存数据
     */
    @Transactional
    public DataInstanceDto save(Long categoryId, Long formId, Object data) {
        // 验证分类
        DataCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException("分类不存在"));

        if (!"form".equals(category.getType())) {
            throw new BusinessException("只能向表单分类添加数据");
        }

        // 验证表单
        Form form = formRepository.findById(formId)
                .orElseThrow(() -> new BusinessException("表单不存在"));

        // 转换数据为JSON
        String dataJson;
        try {
            dataJson = objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new BusinessException("数据格式错误");
        }

        DataInstance instance = new DataInstance();
        instance.setCategoryId(categoryId);
        instance.setFormId(formId);
        instance.setViewId(form.getViewId());
        instance.setDataJson(dataJson);
        instance.setStatus("active");
        instance.setCreatedAt(LocalDateTime.now());

        instance = instanceRepository.save(instance);
        DataInstanceDto dto = convertToDto(instance);
        dto.setFormName(form.getFormName());
        return dto;
    }

    /**
     * 更新数据
     */
    @Transactional
    public DataInstanceDto update(Long id, Object data) {
        DataInstance instance = instanceRepository.findById(id)
                .orElseThrow(() -> new BusinessException("数据不存在"));

        if (!"active".equals(instance.getStatus())) {
            throw new BusinessException("只能修改生效状态的数据");
        }

        // 转换数据为JSON
        String dataJson;
        try {
            dataJson = objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new BusinessException("数据格式错误");
        }

        instance.setDataJson(dataJson);
        instance.setUpdatedAt(LocalDateTime.now());

        instance = instanceRepository.save(instance);
        return convertToDto(instance);
    }

    /**
     * 删除数据
     */
    @Transactional
    public void delete(Long id) {
        DataInstance instance = instanceRepository.findById(id)
                .orElseThrow(() -> new BusinessException("数据不存在"));

        instance.setStatus("obsolete");
        instance.setUpdatedAt(LocalDateTime.now());
        instanceRepository.save(instance);
    }

    /**
     * 转换为DTO
     */
    private DataInstanceDto convertToDto(DataInstance instance) {
        DataInstanceDto dto = new DataInstanceDto();
        dto.setId(instance.getId());
        dto.setCategoryId(instance.getCategoryId());
        dto.setFormId(instance.getFormId());
        dto.setViewId(instance.getViewId());
        dto.setDataJson(instance.getDataJson());
        dto.setStatus(instance.getStatus());
        dto.setCreatedBy(instance.getCreatedBy());
        dto.setUpdatedBy(instance.getUpdatedBy());

        if (instance.getCreatedAt() != null) {
            dto.setCreatedAt(instance.getCreatedAt().toString());
        }
        if (instance.getUpdatedAt() != null) {
            dto.setUpdatedAt(instance.getUpdatedAt().toString());
        }

        // 获取表单名称
        formRepository.findById(instance.getFormId())
                .ifPresent(form -> dto.setFormName(form.getFormName()));

        return dto;
    }
}

package com.vueadmin.service.standard;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vueadmin.dto.DataStandardDto;
import com.vueadmin.entity.standard.DataStandard;
import com.vueadmin.entity.standard.FieldStandard;
import com.vueadmin.repository.DataStandardRepository;
import com.vueadmin.repository.FieldCategoryRepository;
import com.vueadmin.repository.FieldStandardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据标准视图服务
 */
@Service
public class DataStandardService {

    @Autowired
    private DataStandardRepository dataStandardRepository;

    @Autowired
    private FieldStandardRepository fieldStandardRepository;

    @Autowired
    private FieldCategoryRepository fieldCategoryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 分页查询
     */
    public Page<DataStandardDto> search(String keyword, String status,
                                        Long categoryId, Pageable pageable) {
        Page<DataStandard> page = dataStandardRepository.search(keyword, status, categoryId, pageable);
        return page.map(this::toDto);
    }

    /**
     * 根据ID查询
     */
    public DataStandardDto findById(Long id) {
        DataStandard entity = dataStandardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("数据标准不存在: " + id));
        return toDto(entity);
    }

    /**
     * 根据标准编码查询
     */
    public DataStandardDto findByStandardCode(String standardCode) {
        DataStandard entity = dataStandardRepository.findByStandardCode(standardCode)
                .orElseThrow(() -> new RuntimeException("数据标准不存在: " + standardCode));
        return toDto(entity);
    }

    /**
     * 创建数据标准
     */
    @Transactional
    public DataStandardDto create(DataStandardDto dto) {
        // 检查编码唯一性
        if (dataStandardRepository.existsByStandardCode(dto.getStandardCode())) {
            throw new RuntimeException("标准编码已存在: " + dto.getStandardCode());
        }

        DataStandard entity = new DataStandard();
        entity.setStandardCode(dto.getStandardCode());
        entity.setStandardName(dto.getStandardName());
        entity.setCategoryId(dto.getCategoryId());
        entity.setStatus("draft");
        entity.setApprovalStatus("pending");
        entity.setVersion(1);
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setUpdatedBy(dto.getUpdatedBy());
        entity.setDescription(dto.getDescription());

        // 序列化字段定义
        if (dto.getFieldsDefinition() != null) {
            try {
                entity.setFieldsDefinition(objectMapper.writeValueAsString(dto.getFieldsDefinition()));
            } catch (Exception e) {
                throw new RuntimeException("字段定义序列化失败", e);
            }
        }

        DataStandard saved = dataStandardRepository.save(entity);
        return toDto(saved);
    }

    /**
     * 更新数据标准
     */
    @Transactional
    public DataStandardDto update(Long id, DataStandardDto dto) {
        DataStandard entity = dataStandardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("数据标准不存在: " + id));

        // 如果已发布，不允许修改
        if ("published".equals(entity.getStatus())) {
            throw new RuntimeException("已发布的数据标准不能修改，请先创建新版本");
        }

        entity.setStandardName(dto.getStandardName());
        entity.setCategoryId(dto.getCategoryId());
        entity.setUpdatedBy(dto.getUpdatedBy());
        entity.setDescription(dto.getDescription());

        // 序列化字段定义
        if (dto.getFieldsDefinition() != null) {
            try {
                entity.setFieldsDefinition(objectMapper.writeValueAsString(dto.getFieldsDefinition()));
            } catch (Exception e) {
                throw new RuntimeException("字段定义序列化失败", e);
            }
        }

        DataStandard saved = dataStandardRepository.save(entity);
        return toDto(saved);
    }

    /**
     * 删除数据标准
     */
    @Transactional
    public void delete(Long id) {
        DataStandard entity = dataStandardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("数据标准不存在: " + id));

        if ("published".equals(entity.getStatus())) {
            throw new RuntimeException("已发布的数据标准不能删除");
        }

        dataStandardRepository.deleteById(id);
    }

    /**
     * 发布数据标准
     */
    @Transactional
    public DataStandardDto publish(Long id) {
        DataStandard entity = dataStandardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("数据标准不存在: " + id));

        if ("published".equals(entity.getStatus())) {
            throw new RuntimeException("数据标准已处于发布状态");
        }

        entity.setStatus("published");
        entity.setPublishTime(LocalDateTime.now());
        entity.setVersion(entity.getVersion() + 1);
        entity.setUpdatedBy(entity.getUpdatedBy());

        DataStandard saved = dataStandardRepository.save(entity);
        return toDto(saved);
    }

    /**
     * 归档数据标准
     */
    @Transactional
    public DataStandardDto archive(Long id) {
        DataStandard entity = dataStandardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("数据标准不存在: " + id));

        entity.setStatus("archived");
        DataStandard saved = dataStandardRepository.save(entity);
        return toDto(saved);
    }

    /**
     * 恢复数据标准（从归档恢复为已发布）
     */
    @Transactional
    public DataStandardDto restore(Long id) {
        DataStandard entity = dataStandardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("数据标准不存在: " + id));

        if (!"archived".equals(entity.getStatus())) {
            throw new RuntimeException("只有已归档的数据标准才能恢复");
        }

        entity.setStatus("published");
        DataStandard saved = dataStandardRepository.save(entity);
        return toDto(saved);
    }

    /**
     * 审批数据标准
     */
    @Transactional
    public DataStandardDto approve(Long id, String approvalBy, boolean approved, String comment) {
        DataStandard entity = dataStandardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("数据标准不存在: " + id));

        entity.setApprovalStatus(approved ? "approved" : "rejected");
        entity.setApprovalBy(approvalBy);
        entity.setApprovalTime(LocalDateTime.now());
        entity.setApprovalComment(comment);

        DataStandard saved = dataStandardRepository.save(entity);
        return toDto(saved);
    }

    /**
     * 批量删除
     */
    @Transactional
    public void batchDelete(List<Long> ids, String updatedBy) {
        for (Long id : ids) {
            DataStandard entity = dataStandardRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("数据标准不存在: " + id));

            if ("published".equals(entity.getStatus())) {
                throw new RuntimeException("已发布的数据标准不能删除: " + entity.getStandardName());
            }

            dataStandardRepository.deleteById(id);
        }
    }

    /**
     * 统计各状态数量
     */
    public long countByStatus(String status) {
        return dataStandardRepository.countByStatus(status);
    }

    /**
     * 转换为DTO
     */
    private DataStandardDto toDto(DataStandard entity) {
        DataStandardDto dto = new DataStandardDto();
        dto.setId(entity.getId());
        dto.setStandardCode(entity.getStandardCode());
        dto.setStandardName(entity.getStandardName());
        dto.setCategoryId(entity.getCategoryId());
        dto.setStatus(entity.getStatus());
        dto.setVersion(entity.getVersion());
        dto.setPublishTime(entity.getPublishTime());
        dto.setApprovalStatus(entity.getApprovalStatus());
        dto.setApprovalBy(entity.getApprovalBy());
        dto.setApprovalTime(entity.getApprovalTime());
        dto.setApprovalComment(entity.getApprovalComment());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setDescription(entity.getDescription());

        // 获取分类名称
        if (entity.getCategoryId() != null) {
            fieldCategoryRepository.findById(entity.getCategoryId())
                    .ifPresent(category -> dto.setCategoryName(category.getCategoryName()));
        }

        // 解析字段定义
        if (entity.getFieldsDefinition() != null && !entity.getFieldsDefinition().isEmpty()) {
            try {
                List<DataStandardDto.FieldDefinition> fieldDefs = objectMapper.readValue(
                        entity.getFieldsDefinition(),
                        new TypeReference<List<DataStandardDto.FieldDefinition>>() {}
                );

                // 填充字段详情
                if (!fieldDefs.isEmpty()) {
                    List<Long> fieldIds = fieldDefs.stream()
                            .map(DataStandardDto.FieldDefinition::getFieldId)
                            .collect(Collectors.toList());

                    List<FieldStandard> fields = fieldStandardRepository.findAllById(fieldIds);
                    Map<Long, FieldStandard> fieldMap = fields.stream()
                            .collect(Collectors.toMap(FieldStandard::getId, f -> f));

                    for (DataStandardDto.FieldDefinition fd : fieldDefs) {
                        FieldStandard field = fieldMap.get(fd.getFieldId());
                        if (field != null) {
                            fd.setFieldCode(field.getFieldCode());
                            fd.setFieldName(field.getFieldName());
                        }
                    }
                }

                dto.setFieldsDefinition(fieldDefs);
            } catch (Exception e) {
                // 解析失败时设置空列表
                dto.setFieldsDefinition(new ArrayList<>());
            }
        }

        return dto;
    }
}

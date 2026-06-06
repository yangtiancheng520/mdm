package com.vueadmin.service.standard;

import com.vueadmin.dto.FieldStandardDto;
import com.vueadmin.dto.PageResult;
import com.vueadmin.entity.standard.FieldCategory;
import com.vueadmin.entity.standard.FieldStandard;
import com.vueadmin.repository.FieldCategoryRepository;
import com.vueadmin.repository.FieldStandardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 字段标准服务
 * 提供字段标准的业务逻辑处理
 */
@Service
@RequiredArgsConstructor
public class FieldStandardService {

    private final FieldStandardRepository fieldStandardRepository;
    private final FieldCategoryRepository fieldCategoryRepository;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 分页查询字段标准列表
     *
     * @param fieldCode  字段编码（模糊查询）
     * @param fieldName  字段名称（模糊查询）
     * @param fieldType  字段类型
     * @param status     状态
     * @param category   分类（旧字段，兼容）
     * @param categoryId 分类ID
     * @param page       页码
     * @param pageSize   每页大小
     * @return 分页结果
     */
    public PageResult<FieldStandardDto> search(String fieldCode, String fieldName, String fieldType,
                                                String status, String category, Long categoryId,
                                                Integer page, Integer pageSize) {
        // 将空字符串转换为null，避免查询问题
        fieldCode = (fieldCode != null && fieldCode.trim().isEmpty()) ? null : fieldCode;
        fieldName = (fieldName != null && fieldName.trim().isEmpty()) ? null : fieldName;
        fieldType = (fieldType != null && fieldType.trim().isEmpty()) ? null : fieldType;
        status = (status != null && status.trim().isEmpty()) ? null : status;
        category = (category != null && category.trim().isEmpty()) ? null : category;

        // 构建分页参数，默认按创建时间倒序
        Pageable pageable = PageRequest.of(
                page != null ? page - 1 : 0,
                pageSize != null ? pageSize : 10,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<FieldStandard> pageResult = fieldStandardRepository.searchFieldStandards(
                fieldCode, fieldName, fieldType, status, category, categoryId, pageable);

        List<FieldStandardDto> list = pageResult.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        int currentPage = page != null ? page : 1;
        int currentPageSize = pageSize != null ? pageSize : 10;

        return PageResult.of(list, pageResult.getTotalElements(), currentPage, currentPageSize);
    }

    /**
     * 获取所有字段标准列表（不分页）
     *
     * @return 字段标准列表
     */
    public List<FieldStandardDto> getAll() {
        return fieldStandardRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 获取已发布的字段标准列表
     *
     * @return 已发布的字段标准列表
     */
    public List<FieldStandardDto> getPublished() {
        return fieldStandardRepository.findAllPublished().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 根据ID获取字段标准详情
     *
     * @param id 主键ID
     * @return 字段标准DTO
     */
    public FieldStandardDto getById(Long id) {
        return fieldStandardRepository.findById(id)
                .map(this::toDto)
                .orElse(null);
    }

    /**
     * 根据字段编码获取字段标准详情
     *
     * @param fieldCode 字段编码
     * @return 字段标准DTO
     */
    public FieldStandardDto getByFieldCode(String fieldCode) {
        return fieldStandardRepository.findByFieldCode(fieldCode)
                .map(this::toDto)
                .orElse(null);
    }

    /**
     * 根据状态获取字段标准列表
     *
     * @param status 状态
     * @return 字段标准列表
     */
    public List<FieldStandardDto> getByStatus(String status) {
        return fieldStandardRepository.findByStatus(status).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 根据分类获取字段标准列表
     *
     * @param category 分类
     * @return 字段标准列表
     */
    public List<FieldStandardDto> getByCategory(String category) {
        return fieldStandardRepository.findByCategory(category).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 获取所有分类列表
     *
     * @return 分类列表
     */
    public List<String> getAllCategories() {
        return fieldStandardRepository.findAllCategories();
    }

    /**
     * 创建字段标准
     *
     * @param dto 字段标准DTO
     * @return 创建后的字段标准DTO
     */
    @Transactional
    public FieldStandardDto create(FieldStandardDto dto) {
        // 检查字段编码是否已存在
        if (fieldStandardRepository.existsByFieldCode(dto.getFieldCode())) {
            throw new RuntimeException("字段编码已存在: " + dto.getFieldCode());
        }

        FieldStandard entity = new FieldStandard();
        // 设置基本属性
        entity.setFieldCode(dto.getFieldCode());
        entity.setFieldName(dto.getFieldName());
        entity.setFieldType(dto.getFieldType());
        entity.setLength(dto.getLength());
        entity.setPrecision(dto.getPrecision());
        entity.setDefaultValue(dto.getDefaultValue());
        entity.setIsRequired(dto.getIsRequired());
        entity.setValidationRule(dto.getValidationRule());
        entity.setReferenceId(dto.getReferenceId());
        entity.setReferenceSource(dto.getReferenceSource());
        entity.setCategoryId(dto.getCategoryId());
        entity.setCategory(dto.getCategory());
        entity.setDescription(dto.getDescription());
        entity.setCreatedBy(dto.getCreatedBy());

        // 设置默认状态
        if (dto.getStatus() != null) {
            entity.setStatus(dto.getStatus());
        }
        if (dto.getVersion() != null) {
            entity.setVersion(dto.getVersion());
        }

        FieldStandard saved = fieldStandardRepository.save(entity);
        return toDto(saved);
    }

    /**
     * 更新字段标准
     *
     * @param id  主键ID
     * @param dto 字段标准DTO
     * @return 更新后的字段标准DTO
     */
    @Transactional
    public FieldStandardDto update(Long id, FieldStandardDto dto) {
        FieldStandard entity = fieldStandardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("字段标准不存在: " + id));

        // 如果字段编码变更，检查新编码是否已存在
        if (!entity.getFieldCode().equals(dto.getFieldCode())) {
            if (fieldStandardRepository.existsByFieldCode(dto.getFieldCode())) {
                throw new RuntimeException("字段编码已存在: " + dto.getFieldCode());
            }
            entity.setFieldCode(dto.getFieldCode());
        }

        // 更新基本属性
        entity.setFieldName(dto.getFieldName());
        entity.setFieldType(dto.getFieldType());
        entity.setLength(dto.getLength());
        entity.setPrecision(dto.getPrecision());
        entity.setDefaultValue(dto.getDefaultValue());
        entity.setIsRequired(dto.getIsRequired());
        entity.setValidationRule(dto.getValidationRule());
        entity.setReferenceId(dto.getReferenceId());
        entity.setReferenceSource(dto.getReferenceSource());
        entity.setCategoryId(dto.getCategoryId());
        entity.setCategory(dto.getCategory());
        entity.setDescription(dto.getDescription());
        entity.setUpdatedBy(dto.getUpdatedBy());

        FieldStandard saved = fieldStandardRepository.save(entity);
        return toDto(saved);
    }

    /**
     * 删除字段标准
     *
     * @param id 主键ID
     */
    @Transactional
    public void delete(Long id) {
        FieldStandard entity = fieldStandardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("字段标准不存在: " + id));

        // 如果已发布，不允许删除
        if ("published".equals(entity.getStatus())) {
            throw new RuntimeException("已发布的字段标准不能删除，请先归档");
        }

        fieldStandardRepository.deleteById(id);
    }

    /**
     * 发布字段标准
     *
     * @param id 主键ID
     * @return 更新后的字段标准DTO
     */
    @Transactional
    public FieldStandardDto publish(Long id) {
        FieldStandard entity = fieldStandardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("字段标准不存在: " + id));

        // 检查当前状态
        if ("published".equals(entity.getStatus())) {
            throw new RuntimeException("字段标准已处于发布状态");
        }
        if ("archived".equals(entity.getStatus())) {
            throw new RuntimeException("已归档的字段标准不能发布");
        }

        entity.setStatus("published");
        FieldStandard saved = fieldStandardRepository.save(entity);
        return toDto(saved);
    }

    /**
     * 归档字段标准
     *
     * @param id 主键ID
     * @return 更新后的字段标准DTO
     */
    @Transactional
    public FieldStandardDto archive(Long id) {
        FieldStandard entity = fieldStandardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("字段标准不存在: " + id));

        // 检查当前状态
        if ("archived".equals(entity.getStatus())) {
            throw new RuntimeException("字段标准已处于归档状态");
        }

        entity.setStatus("archived");
        FieldStandard saved = fieldStandardRepository.save(entity);
        return toDto(saved);
    }

    /**
     * 取消发布（将已发布的字段标准改回草稿状态）
     *
     * @param id 主键ID
     * @return 更新后的字段标准DTO
     */
    @Transactional
    public FieldStandardDto unpublish(Long id) {
        FieldStandard entity = fieldStandardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("字段标准不存在: " + id));

        if (!"published".equals(entity.getStatus())) {
            throw new RuntimeException("只有已发布的字段标准才能取消发布");
        }

        entity.setStatus("draft");
        FieldStandard saved = fieldStandardRepository.save(entity);
        return toDto(saved);
    }

    /**
     * 批量更新分类
     *
     * @param ids      ID列表
     * @param category 分类
     * @param updatedBy 更新人
     */
    @Transactional
    public void batchUpdateCategory(List<Long> ids, String category, String updatedBy) {
        for (Long id : ids) {
            fieldStandardRepository.findById(id).ifPresent(entity -> {
                entity.setCategory(category);
                entity.setUpdatedBy(updatedBy);
                fieldStandardRepository.save(entity);
            });
        }
    }

    /**
     * 批量删除字段标准
     *
     * @param ids       字段标准ID列表
     * @param updatedBy 更新人
     */
    @Transactional
    public void batchDelete(List<Long> ids, String updatedBy) {
        for (Long id : ids) {
            FieldStandard entity = fieldStandardRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("字段标准不存在: " + id));

            // 如果已发布，不允许删除
            if ("published".equals(entity.getStatus())) {
                throw new RuntimeException("已发布的字段标准不能删除，请先归档");
            }

            fieldStandardRepository.deleteById(id);
        }
    }

    /**
     * 获取字段类型统计
     *
     * @return 统计结果
     */
    public long countByStatus(String status) {
        return fieldStandardRepository.countByStatus(status);
    }

    /**
     * 转换为DTO
     *
     * @param entity 实体对象
     * @return DTO对象
     */
    private FieldStandardDto toDto(FieldStandard entity) {
        FieldStandardDto dto = new FieldStandardDto();
        dto.setId(entity.getId());
        dto.setFieldCode(entity.getFieldCode());
        dto.setFieldName(entity.getFieldName());
        dto.setFieldType(entity.getFieldType());
        dto.setLength(entity.getLength());
        dto.setPrecision(entity.getPrecision());
        dto.setDefaultValue(entity.getDefaultValue());
        dto.setIsRequired(entity.getIsRequired());
        dto.setValidationRule(entity.getValidationRule());
        dto.setReferenceId(entity.getReferenceId());
        dto.setReferenceSource(entity.getReferenceSource());
        dto.setCategoryId(entity.getCategoryId());
        dto.setCategory(entity.getCategory());

        // 获取分类名称
        if (entity.getCategoryId() != null) {
            fieldCategoryRepository.findById(entity.getCategoryId())
                    .ifPresent(category -> dto.setCategoryName(category.getCategoryName()));
        }

        dto.setStatus(entity.getStatus());
        dto.setVersion(entity.getVersion());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setDescription(entity.getDescription());

        if (entity.getCreatedAt() != null) {
            dto.setCreatedAt(entity.getCreatedAt().format(formatter));
        }
        if (entity.getUpdatedAt() != null) {
            dto.setUpdatedAt(entity.getUpdatedAt().format(formatter));
        }

        return dto;
    }
}

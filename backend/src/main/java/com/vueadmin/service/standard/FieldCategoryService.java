package com.vueadmin.service.standard;

import com.vueadmin.dto.FieldCategoryDto;
import com.vueadmin.entity.standard.FieldCategory;
import com.vueadmin.exception.BusinessException;
import com.vueadmin.exception.ErrorCode;
import com.vueadmin.repository.FieldCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 字段分类服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FieldCategoryService {

    private final FieldCategoryRepository fieldCategoryRepository;

    /**
     * 获取分类树
     */
    public List<FieldCategoryDto> getCategoryTree() {
        List<FieldCategory> allCategories = fieldCategoryRepository.findAllByOrderBySortAsc();
        return buildTree(allCategories, null);
    }

    /**
     * 获取启用的分类树
     */
    public List<FieldCategoryDto> getActiveCategoryTree() {
        List<FieldCategory> allCategories = fieldCategoryRepository.findByStatusOrderBySortAsc("active");
        return buildTree(allCategories, null);
    }

    /**
     * 构建树形结构
     */
    private List<FieldCategoryDto> buildTree(List<FieldCategory> allCategories, Long parentId) {
        Map<Long, List<FieldCategory>> categoryMap = allCategories.stream()
                .collect(Collectors.groupingBy(c -> c.getParentId() == null ? 0L : c.getParentId()));

        List<FieldCategory> children = categoryMap.getOrDefault(parentId == null ? 0L : parentId, new ArrayList<>());

        return children.stream().map(category -> {
            FieldCategoryDto dto = convertToDto(category);
            List<FieldCategoryDto> childList = buildTree(allCategories, category.getId());
            dto.setChildren(childList.isEmpty() ? null : childList);
            dto.setHasChildren(!childList.isEmpty());
            dto.setLabel(category.getCategoryName());
            dto.setValue(category.getId());
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * 获取分类列表（平铺）
     */
    public List<FieldCategoryDto> getCategoryList() {
        List<FieldCategory> categories = fieldCategoryRepository.findAllByOrderBySortAsc();
        return categories.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    /**
     * 根据ID获取分类
     */
    public FieldCategoryDto getById(Long id) {
        FieldCategory category = fieldCategoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND, "分类不存在"));
        FieldCategoryDto dto = convertToDto(category);
        dto.setHasChildren(fieldCategoryRepository.existsByParentId(id));
        return dto;
    }

    /**
     * 获取子分类
     */
    public List<FieldCategoryDto> getChildren(Long parentId) {
        List<FieldCategory> children = fieldCategoryRepository.findByParentIdOrderBySortAsc(parentId);
        return children.stream().map(category -> {
            FieldCategoryDto dto = convertToDto(category);
            dto.setHasChildren(fieldCategoryRepository.existsByParentId(category.getId()));
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * 创建分类
     */
    @Transactional
    public FieldCategoryDto create(FieldCategoryDto dto) {
        // 检查编码是否已存在
        if (fieldCategoryRepository.existsByCategoryCode(dto.getCategoryCode())) {
            throw new BusinessException(ErrorCode.DATA_ALREADY_EXISTS, "分类编码已存在");
        }

        FieldCategory category = new FieldCategory();
        category.setCategoryCode(dto.getCategoryCode());
        category.setCategoryName(dto.getCategoryName());
        category.setParentId(dto.getParentId());
        category.setSort(dto.getSort() != null ? dto.getSort() : 0);
        category.setStatus(dto.getStatus() != null ? dto.getStatus() : "active");
        category.setDescription(dto.getDescription());
        category.setCreatedBy(dto.getCreatedBy());

        FieldCategory saved = fieldCategoryRepository.save(category);
        log.info("创建字段分类: {}", saved.getCategoryName());
        return convertToDto(saved);
    }

    /**
     * 更新分类
     */
    @Transactional
    public FieldCategoryDto update(Long id, FieldCategoryDto dto) {
        FieldCategory category = fieldCategoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND, "分类不存在"));

        // 检查编码是否被其他分类使用
        if (!category.getCategoryCode().equals(dto.getCategoryCode())
                && fieldCategoryRepository.existsByCategoryCodeAndIdNot(dto.getCategoryCode(), id)) {
            throw new BusinessException(ErrorCode.DATA_ALREADY_EXISTS, "分类编码已存在");
        }

        category.setCategoryCode(dto.getCategoryCode());
        category.setCategoryName(dto.getCategoryName());
        category.setSort(dto.getSort());
        category.setStatus(dto.getStatus());
        category.setDescription(dto.getDescription());
        category.setUpdatedBy(dto.getUpdatedBy());

        // 更新父级ID
        if ((category.getParentId() == null && dto.getParentId() != null)
                || (category.getParentId() != null && !category.getParentId().equals(dto.getParentId()))) {
            // 检查是否将分类移动到自己的子分类下
            if (dto.getParentId() != null && isChildOf(dto.getParentId(), id)) {
                throw new BusinessException(ErrorCode.DATA_STATUS_ERROR, "不能将分类移动到自己的子分类下");
            }
            category.setParentId(dto.getParentId());
        }

        FieldCategory saved = fieldCategoryRepository.save(category);
        log.info("更新字段分类: {}", saved.getCategoryName());
        return convertToDto(saved);
    }

    /**
     * 删除分类
     */
    @Transactional
    public void delete(Long id) {
        FieldCategory category = fieldCategoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND, "分类不存在"));

        // 检查是否有子分类
        boolean hasChildren = fieldCategoryRepository.existsByParentId(id);
        if (hasChildren) {
            throw new BusinessException(ErrorCode.DATA_STATUS_ERROR, "存在子分类，无法删除");
        }

        // 检查是否有关联字段
        long fieldCount = fieldCategoryRepository.countFieldsByCategoryId(id);
        if (fieldCount > 0) {
            throw new BusinessException(ErrorCode.DATA_STATUS_ERROR,
                    String.format("该分类下有 %d 个字段，请先迁移字段", fieldCount));
        }

        fieldCategoryRepository.delete(category);
        log.info("删除字段分类: {}", category.getCategoryName());
    }

    /**
     * 批量删除
     */
    @Transactional
    public void batchDelete(List<Long> ids) {
        for (Long id : ids) {
            delete(id);
        }
    }

    /**
     * 获取分类下的字段数量
     */
    public long getFieldCount(Long categoryId) {
        return fieldCategoryRepository.countFieldsByCategoryId(categoryId);
    }

    /**
     * 检查指定ID是否是另一个ID的子分类
     */
    private boolean isChildOf(Long childId, Long parentId) {
        FieldCategory child = fieldCategoryRepository.findById(childId).orElse(null);
        while (child != null && child.getParentId() != null) {
            if (child.getParentId().equals(parentId)) {
                return true;
            }
            child = fieldCategoryRepository.findById(child.getParentId()).orElse(null);
        }
        return false;
    }

    /**
     * 转换为DTO
     */
    private FieldCategoryDto convertToDto(FieldCategory category) {
        FieldCategoryDto dto = new FieldCategoryDto();
        dto.setId(category.getId());
        dto.setCategoryCode(category.getCategoryCode());
        dto.setCategoryName(category.getCategoryName());
        dto.setParentId(category.getParentId());
        dto.setSort(category.getSort());
        dto.setStatus(category.getStatus());
        dto.setCreatedBy(category.getCreatedBy());
        dto.setCreatedAt(category.getCreatedAt());
        dto.setUpdatedBy(category.getUpdatedBy());
        dto.setUpdatedAt(category.getUpdatedAt());
        dto.setDescription(category.getDescription());
        dto.setLabel(category.getCategoryName());
        dto.setValue(category.getId());
        return dto;
    }
}

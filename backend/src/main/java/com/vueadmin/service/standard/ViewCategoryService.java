package com.vueadmin.service.standard;

import com.vueadmin.dto.ViewCategoryDto;
import com.vueadmin.entity.standard.ViewCategory;
import com.vueadmin.repository.ViewCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 视图分类服务
 */
@Service
public class ViewCategoryService {

    @Autowired
    private ViewCategoryRepository viewCategoryRepository;

    /**
     * 获取分类树
     */
    public List<ViewCategoryDto> getTree() {
        List<ViewCategory> all = viewCategoryRepository.findAllByOrderBySortAsc();
        return buildTree(all, null);
    }

    /**
     * 获取启用的分类树
     */
    public List<ViewCategoryDto> getActiveTree() {
        List<ViewCategory> all = viewCategoryRepository.findAllActive();
        return buildTree(all, null);
    }

    /**
     * 构建树结构
     */
    private List<ViewCategoryDto> buildTree(List<ViewCategory> all, Long parentId) {
        List<ViewCategoryDto> tree = new ArrayList<>();
        for (ViewCategory entity : all) {
            if ((parentId == null && entity.getParentId() == null) ||
                (parentId != null && parentId.equals(entity.getParentId()))) {
                ViewCategoryDto dto = toDto(entity);
                dto.setChildren(buildTree(all, entity.getId()));
                dto.setHasChildren(!dto.getChildren().isEmpty());
                tree.add(dto);
            }
        }
        return tree;
    }

    /**
     * 获取所有分类（扁平列表）
     */
    public List<ViewCategoryDto> getAll() {
        List<ViewCategory> all = viewCategoryRepository.findAllByOrderBySortAsc();
        return all.stream().map(this::toDto).collect(Collectors.toList());
    }

    /**
     * 根据ID查询
     */
    public ViewCategoryDto findById(Long id) {
        ViewCategory entity = viewCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("分类不存在: " + id));
        return toDto(entity);
    }

    /**
     * 创建分类
     */
    @Transactional
    public ViewCategoryDto create(ViewCategoryDto dto) {
        if (viewCategoryRepository.existsByCategoryCode(dto.getCategoryCode())) {
            throw new RuntimeException("分类编码已存在: " + dto.getCategoryCode());
        }

        ViewCategory entity = new ViewCategory();
        entity.setCategoryCode(dto.getCategoryCode());
        entity.setCategoryName(dto.getCategoryName());
        entity.setParentId(dto.getParentId());
        entity.setSort(dto.getSort() != null ? dto.getSort() : 0);
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : "active");
        entity.setDescription(dto.getDescription());
        entity.setCreatedBy(dto.getCreatedBy());

        ViewCategory saved = viewCategoryRepository.save(entity);
        return toDto(saved);
    }

    /**
     * 更新分类
     */
    @Transactional
    public ViewCategoryDto update(Long id, ViewCategoryDto dto) {
        ViewCategory entity = viewCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("分类不存在: " + id));

        // 检查编码唯一性
        if (!entity.getCategoryCode().equals(dto.getCategoryCode()) &&
            viewCategoryRepository.existsByCategoryCodeAndIdNot(dto.getCategoryCode(), id)) {
            throw new RuntimeException("分类编码已存在: " + dto.getCategoryCode());
        }

        // 不能将自己设为父级
        if (id.equals(dto.getParentId())) {
            throw new RuntimeException("不能将自己设为父级分类");
        }

        entity.setCategoryCode(dto.getCategoryCode());
        entity.setCategoryName(dto.getCategoryName());
        entity.setParentId(dto.getParentId());
        entity.setSort(dto.getSort());
        entity.setStatus(dto.getStatus());
        entity.setDescription(dto.getDescription());
        entity.setUpdatedBy(dto.getUpdatedBy());

        ViewCategory saved = viewCategoryRepository.save(entity);
        return toDto(saved);
    }

    /**
     * 删除分类
     */
    @Transactional
    public void delete(Long id) {
        ViewCategory entity = viewCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("分类不存在: " + id));

        // 检查是否有子分类
        if (viewCategoryRepository.existsByParentId(id)) {
            throw new RuntimeException("该分类下有子分类，不能删除");
        }

        // 检查是否有关联的视图
        long count = viewCategoryRepository.countViewsByCategoryId(id);
        if (count > 0) {
            throw new RuntimeException("该分类下有 " + count + " 个视图，不能删除");
        }

        viewCategoryRepository.deleteById(id);
    }

    /**
     * 获取分类下的视图数量
     */
    public long getViewCount(Long id) {
        return viewCategoryRepository.countViewsByCategoryId(id);
    }

    /**
     * 转换为DTO
     */
    private ViewCategoryDto toDto(ViewCategory entity) {
        ViewCategoryDto dto = new ViewCategoryDto();
        dto.setId(entity.getId());
        dto.setCategoryCode(entity.getCategoryCode());
        dto.setCategoryName(entity.getCategoryName());
        dto.setParentId(entity.getParentId());
        dto.setSort(entity.getSort());
        dto.setStatus(entity.getStatus());
        dto.setDescription(entity.getDescription());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedAt(entity.getUpdatedAt());

        // 获取视图数量
        dto.setViewCount(viewCategoryRepository.countViewsByCategoryId(entity.getId()));

        return dto;
    }
}

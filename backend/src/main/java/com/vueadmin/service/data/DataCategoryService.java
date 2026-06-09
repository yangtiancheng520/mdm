package com.vueadmin.service.data;

import com.vueadmin.dto.DataCategoryDto;
import com.vueadmin.entity.data.DataCategory;
import com.vueadmin.entity.form.Form;
import com.vueadmin.exception.BusinessException;
import com.vueadmin.repository.DataCategoryRepository;
import com.vueadmin.repository.FormRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据分类Service
 */
@Service
@RequiredArgsConstructor
public class DataCategoryService {

    private final DataCategoryRepository categoryRepository;
    private final FormRepository formRepository;

    /**
     * 获取分类树
     */
    public List<DataCategoryDto> getCategoryTree() {
        List<DataCategory> all = categoryRepository.findAllOrderBySort();
        return buildTree(all, null);
    }

    /**
     * 构建树结构
     */
    private List<DataCategoryDto> buildTree(List<DataCategory> all, Long parentId) {
        List<DataCategoryDto> result = new ArrayList<>();

        // 获取表单名称映射
        List<Long> formIds = all.stream()
                .filter(c -> "form".equals(c.getType()) && c.getFormId() != null)
                .map(DataCategory::getFormId)
                .collect(Collectors.toList());
        Map<Long, String> formNameMap = new java.util.HashMap<>();
        if (!formIds.isEmpty()) {
            List<Form> forms = formRepository.findAllById(formIds);
            for (Form form : forms) {
                if (form.getId() != null && form.getFormName() != null) {
                    formNameMap.put(form.getId(), form.getFormName());
                }
            }
        }

        for (DataCategory category : all) {
            boolean isChild = (parentId == null && category.getParentId() == null) ||
                    (parentId != null && parentId.equals(category.getParentId()));

            if (isChild) {
                DataCategoryDto dto = convertToDto(category);
                if (category.getFormId() != null) {
                    dto.setFormName(formNameMap.get(category.getFormId()));
                }

                // 递归查找子节点
                List<DataCategoryDto> children = buildTree(all, category.getId());
                dto.setChildren(children.isEmpty() ? null : children);

                result.add(dto);
            }
        }

        return result;
    }

    /**
     * 创建文件夹
     */
    @Transactional
    public DataCategoryDto createFolder(Long parentId, String name, String icon) {
        DataCategory category = new DataCategory();
        category.setParentId(parentId);
        category.setName(name);
        category.setType("folder");
        category.setIcon(icon);
        category.setSort(getNextSort(parentId));
        category.setCreatedAt(LocalDateTime.now());

        category = categoryRepository.save(category);
        return convertToDto(category);
    }

    /**
     * 添加表单
     */
    @Transactional
    public DataCategoryDto addForm(Long parentId, Long formId) {
        // 检查表单是否存在且已发布
        Form form = formRepository.findById(formId)
                .orElseThrow(() -> new BusinessException("表单不存在"));

        if (!"published".equals(form.getStatus())) {
            throw new BusinessException("只能添加已发布的表单");
        }

        // 检查父级是否为文件夹
        if (parentId != null) {
            DataCategory parent = categoryRepository.findById(parentId)
                    .orElseThrow(() -> new BusinessException("父级分类不存在"));
            if (!"folder".equals(parent.getType())) {
                throw new BusinessException("只能将表单添加到文件夹下");
            }
        }

        DataCategory category = new DataCategory();
        category.setParentId(parentId);
        category.setName(form.getFormName());
        category.setType("form");
        category.setFormId(formId);
        category.setSort(getNextSort(parentId));
        category.setCreatedAt(LocalDateTime.now());

        category = categoryRepository.save(category);
        DataCategoryDto dto = convertToDto(category);
        dto.setFormName(form.getFormName());
        return dto;
    }

    /**
     * 更新分类
     */
    @Transactional
    public DataCategoryDto updateCategory(Long id, String name, String icon) {
        DataCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException("分类不存在"));

        category.setName(name);
        category.setIcon(icon);
        category.setUpdatedAt(LocalDateTime.now());

        category = categoryRepository.save(category);
        return convertToDto(category);
    }

    /**
     * 删除分类（支持递归删除子项）
     */
    @Transactional
    public void deleteCategory(Long id) {
        DataCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException("分类不存在"));

        if ("folder".equals(category.getType())) {
            // 递归删除所有子项
            deleteChildrenRecursively(id);
        }

        categoryRepository.deleteById(id);
    }

    /**
     * 递归删除子项
     */
    private void deleteChildrenRecursively(Long parentId) {
        List<DataCategory> children = categoryRepository.findByParentId(parentId);
        for (DataCategory child : children) {
            if ("folder".equals(child.getType())) {
                deleteChildrenRecursively(child.getId());
            }
            categoryRepository.deleteById(child.getId());
        }
    }

    /**
     * 更新排序
     */
    @Transactional
    public void updateSort(List<Map<String, Object>> items) {
        for (Map<String, Object> item : items) {
            Long id = Long.valueOf(item.get("id").toString());
            Integer sort = Integer.valueOf(item.get("sort").toString());
            Object parentIdObj = item.get("parentId");
            Long parentId = parentIdObj != null ? Long.valueOf(parentIdObj.toString()) : null;

            DataCategory category = categoryRepository.findById(id)
                    .orElseThrow(() -> new BusinessException("分类不存在: " + id));
            category.setSort(sort);
            category.setParentId(parentId);
            category.setUpdatedAt(LocalDateTime.now());
            categoryRepository.save(category);
        }
    }

    /**
     * 获取下一个排序号
     */
    private Integer getNextSort(Long parentId) {
        List<DataCategory> siblings;
        if (parentId == null) {
            siblings = categoryRepository.findRootCategories();
        } else {
            siblings = categoryRepository.findByParentId(parentId);
        }
        return siblings.size();
    }

    /**
     * 转换为DTO
     */
    private DataCategoryDto convertToDto(DataCategory category) {
        DataCategoryDto dto = new DataCategoryDto();
        dto.setId(category.getId());
        dto.setParentId(category.getParentId());
        dto.setName(category.getName());
        dto.setType(category.getType());
        dto.setFormId(category.getFormId());
        dto.setIcon(category.getIcon());
        dto.setSort(category.getSort());
        return dto;
    }
}

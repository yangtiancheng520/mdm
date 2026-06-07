package com.vueadmin.service.form;

import com.vueadmin.dto.FormComponentDto;
import com.vueadmin.dto.FormDesignRequest;
import com.vueadmin.dto.FormDto;
import com.vueadmin.dto.FormGroupDto;
import com.vueadmin.entity.form.Form;
import com.vueadmin.entity.form.FormComponent;
import com.vueadmin.entity.form.FormGroup;
import com.vueadmin.entity.standard.ViewDefinition;
import com.vueadmin.exception.BusinessException;
import com.vueadmin.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FormService {

    private final FormRepository formRepository;
    private final FormGroupRepository formGroupRepository;
    private final FormComponentRepository formComponentRepository;
    private final ViewDefinitionRepository viewDefinitionRepository;

    /**
     * 获取表单列表
     */
    public List<FormDto> getFormList(String formName, String formType, Long viewId, String status) {
        List<Form> forms = formRepository.search(formName, formType, viewId, status);
        return forms.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    /**
     * 获取表单详情
     */
    public FormDto getFormById(Long id) {
        Form form = formRepository.findById(id)
                .orElseThrow(() -> new BusinessException("表单不存在"));
        FormDto dto = convertToDto(form);

        // 获取视图名称
        if (form.getViewId() != null) {
            viewDefinitionRepository.findById(form.getViewId())
                    .ifPresent(view -> dto.setViewName(view.getViewName()));
        }

        return dto;
    }

    /**
     * 获取表单设计数据（包含分组和组件）
     */
    public FormDesignRequest getFormDesign(Long id) {
        Form form = formRepository.findById(id)
                .orElseThrow(() -> new BusinessException("表单不存在"));

        FormDesignRequest design = new FormDesignRequest();
        design.setId(form.getId());
        design.setFormCode(form.getFormCode());
        design.setFormName(form.getFormName());
        design.setFormType(form.getFormType());
        design.setViewId(form.getViewId());
        design.setDesignMode(form.getDesignMode());
        design.setStyleTemplate(form.getStyleTemplate());
        design.setLayoutConfig(form.getLayoutConfig());
        design.setEnableCopy(form.getEnableCopy());
        design.setEnableImport(form.getEnableImport());
        design.setEnableExport(form.getEnableExport());
        design.setDescription(form.getDescription());

        // 获取分组
        List<FormGroup> groups = formGroupRepository.findByFormIdOrderBySort(id);
        design.setGroups(groups.stream().map(this::convertGroupToDto).collect(Collectors.toList()));

        // 获取组件
        List<FormComponent> components = formComponentRepository.findByFormIdOrdered(id);
        design.setComponents(components.stream().map(this::convertComponentToDto).collect(Collectors.toList()));

        return design;
    }

    /**
     * 创建表单
     */
    @Transactional
    public FormDto createForm(FormDesignRequest request) {
        // 生成表单编码
        String formCode = request.getFormCode();
        if (formCode == null || formCode.isEmpty()) {
            formCode = generateFormCode(request.getViewId(), request.getFormType());
        }

        // 校验编码唯一性
        if (formRepository.existsByFormCode(formCode)) {
            throw new BusinessException("表单编码已存在");
        }

        Form form = new Form();
        form.setFormCode(formCode);
        form.setFormName(request.getFormName());
        form.setFormType(request.getFormType());
        form.setViewId(request.getViewId());
        form.setDesignMode("blank");
        form.setDescription(request.getDescription());
        form.setStatus("draft");

        form = formRepository.save(form);
        return convertToDto(form);
    }

    /**
     * 更新表单
     */
    @Transactional
    public FormDto updateForm(Long id, FormDesignRequest request) {
        Form form = formRepository.findById(id)
                .orElseThrow(() -> new BusinessException("表单不存在"));

        form.setFormName(request.getFormName());
        form.setFormType(request.getFormType());
        form.setViewId(request.getViewId());
        form.setDescription(request.getDescription());

        form = formRepository.save(form);
        return convertToDto(form);
    }

    /**
     * 删除表单
     */
    @Transactional
    public void deleteForm(Long id) {
        formComponentRepository.deleteByFormId(id);
        formGroupRepository.deleteByFormId(id);
        formRepository.deleteById(id);
    }

    /**
     * 发布表单
     */
    @Transactional
    public void publishForm(Long id) {
        Form form = formRepository.findById(id)
                .orElseThrow(() -> new BusinessException("表单不存在"));
        form.setStatus("published");
        formRepository.save(form);
    }

    /**
     * 取消发布表单
     */
    @Transactional
    public void unpublishForm(Long id) {
        Form form = formRepository.findById(id)
                .orElseThrow(() -> new BusinessException("表单不存在"));

        if (!"published".equals(form.getStatus())) {
            throw new BusinessException("只有已发布的表单才能取消发布");
        }

        form.setStatus("draft");
        form.setIsDefault(false); // 取消发布时清除默认标记
        formRepository.save(form);
    }

    /**
     * 设置默认表单
     */
    @Transactional
    public void setDefaultForm(Long id) {
        Form form = formRepository.findById(id)
                .orElseThrow(() -> new BusinessException("表单不存在"));

        // 清除同视图同类型的其他默认表单
        if (form.getViewId() != null) {
            formRepository.findDefaultForm(form.getViewId(), form.getFormType())
                    .ifPresent(existingDefault -> {
                        existingDefault.setIsDefault(false);
                        formRepository.save(existingDefault);
                    });
        }

        form.setIsDefault(true);
        formRepository.save(form);
    }

    /**
     * 保存表单设计
     */
    @Transactional
    public void saveFormDesign(FormDesignRequest request) {
        Form form = formRepository.findById(request.getId())
                .orElseThrow(() -> new BusinessException("表单不存在"));

        // 更新表单基本信息
        form.setFormName(request.getFormName());
        form.setStyleTemplate(request.getStyleTemplate());
        form.setLayoutConfig(request.getLayoutConfig());
        form.setEnableCopy(request.getEnableCopy());
        form.setEnableImport(request.getEnableImport());
        form.setEnableExport(request.getEnableExport());
        form.setDescription(request.getDescription());

        formRepository.save(form);

        // 删除旧的分组和组件
        formComponentRepository.deleteByFormId(form.getId());
        formGroupRepository.deleteByFormId(form.getId());

        // 保存分组
        if (request.getGroups() != null) {
            for (FormGroupDto groupDto : request.getGroups()) {
                FormGroup group = new FormGroup();
                group.setFormId(form.getId());
                group.setGroupCode(groupDto.getGroupCode());
                group.setGroupName(groupDto.getGroupName());
                group.setGroupType(groupDto.getGroupType());
                group.setSort(groupDto.getSort());
                group.setIsCollapsed(groupDto.getIsCollapsed());
                formGroupRepository.save(group);
            }
        }

        // 保存组件
        if (request.getComponents() != null) {
            Map<String, Long> groupIdMap = new HashMap<>();
            if (request.getGroups() != null) {
                List<FormGroup> savedGroups = formGroupRepository.findByFormIdOrderBySort(form.getId());
                for (int i = 0; i < savedGroups.size() && i < request.getGroups().size(); i++) {
                    groupIdMap.put(request.getGroups().get(i).getGroupCode(), savedGroups.get(i).getId());
                }
            }

            for (FormComponentDto compDto : request.getComponents()) {
                FormComponent component = new FormComponent();
                component.setFormId(form.getId());
                component.setViewFieldId(compDto.getViewFieldId());
                component.setFieldCode(compDto.getFieldCode());
                component.setFieldName(compDto.getFieldName());
                component.setFieldType(compDto.getFieldType());
                component.setDomainId(compDto.getDomainId());

                if (compDto.getGroupId() != null && groupIdMap.containsValue(compDto.getGroupId())) {
                    component.setGroupId(compDto.getGroupId());
                }

                component.setRowIndex(compDto.getRowIndex() != null ? compDto.getRowIndex() : 0);
                component.setColIndex(compDto.getColIndex() != null ? compDto.getColIndex() : 0);
                component.setColSpan(compDto.getColSpan() != null ? compDto.getColSpan() : 1);
                component.setRowSpan(compDto.getRowSpan() != null ? compDto.getRowSpan() : 1);
                component.setIsRequired(compDto.getIsRequired() != null ? compDto.getIsRequired() : false);
                component.setIsReadonly(compDto.getIsReadonly() != null ? compDto.getIsReadonly() : false);
                component.setIsHidden(compDto.getIsHidden() != null ? compDto.getIsHidden() : false);
                component.setDefaultValue(compDto.getDefaultValue());
                component.setPlaceholder(compDto.getPlaceholder());
                component.setValidationRules(compDto.getValidationRules());
                component.setLabelWidth(compDto.getLabelWidth());
                component.setComponentWidth(compDto.getComponentWidth());
                component.setSort(compDto.getSort() != null ? compDto.getSort() : 0);

                formComponentRepository.save(component);
            }
        }
    }

    // ========== 私有方法 ==========

    private String generateFormCode(Long viewId, String formType) {
        String prefix = "FORM";
        if (viewId != null) {
            prefix = "V" + viewId;
        }
        String typeSuffix = switch (formType) {
            case "create" -> "C";
            case "edit" -> "E";
            case "view" -> "V";
            case "search" -> "S";
            default -> "X";
        };
        return prefix + "_" + typeSuffix + "_" + System.currentTimeMillis() % 10000;
    }

    private FormDto convertToDto(Form form) {
        FormDto dto = new FormDto();
        dto.setId(form.getId());
        dto.setFormCode(form.getFormCode());
        dto.setFormName(form.getFormName());
        dto.setFormType(form.getFormType());
        dto.setViewId(form.getViewId());
        dto.setDesignMode(form.getDesignMode());
        dto.setStyleTemplate(form.getStyleTemplate());
        dto.setLayoutConfig(form.getLayoutConfig());
        dto.setEnableCopy(form.getEnableCopy());
        dto.setEnableImport(form.getEnableImport());
        dto.setEnableExport(form.getEnableExport());
        dto.setStatus(form.getStatus());
        dto.setIsDefault(form.getIsDefault());
        dto.setVersion(form.getVersion());
        dto.setCreatedBy(form.getCreatedBy());
        dto.setCreatedAt(form.getCreatedAt());
        dto.setUpdatedBy(form.getUpdatedBy());
        dto.setUpdatedAt(form.getUpdatedAt());
        dto.setDescription(form.getDescription());
        return dto;
    }

    private FormGroupDto convertGroupToDto(FormGroup group) {
        FormGroupDto dto = new FormGroupDto();
        dto.setId(group.getId());
        dto.setFormId(group.getFormId());
        dto.setGroupCode(group.getGroupCode());
        dto.setGroupName(group.getGroupName());
        dto.setGroupType(group.getGroupType());
        dto.setSort(group.getSort());
        dto.setIsCollapsed(group.getIsCollapsed());
        return dto;
    }

    private FormComponentDto convertComponentToDto(FormComponent component) {
        FormComponentDto dto = new FormComponentDto();
        dto.setId(component.getId());
        dto.setFormId(component.getFormId());
        dto.setViewFieldId(component.getViewFieldId());
        dto.setFieldCode(component.getFieldCode());
        dto.setFieldName(component.getFieldName());
        dto.setFieldType(component.getFieldType());
        dto.setDomainId(component.getDomainId());
        dto.setGroupId(component.getGroupId());
        dto.setRowIndex(component.getRowIndex());
        dto.setColIndex(component.getColIndex());
        dto.setColSpan(component.getColSpan());
        dto.setRowSpan(component.getRowSpan());
        dto.setIsRequired(component.getIsRequired());
        dto.setIsReadonly(component.getIsReadonly());
        dto.setIsHidden(component.getIsHidden());
        dto.setDefaultValue(component.getDefaultValue());
        dto.setPlaceholder(component.getPlaceholder());
        dto.setValidationRules(component.getValidationRules());
        dto.setLabelWidth(component.getLabelWidth());
        dto.setComponentWidth(component.getComponentWidth());
        dto.setSort(component.getSort());
        dto.setStatus(component.getStatus());
        return dto;
    }
}

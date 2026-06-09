package com.vueadmin.service.quality;

import com.vueadmin.dto.QualityRuleTemplateDto;
import com.vueadmin.entity.quality.QualityRuleTemplate;
import com.vueadmin.exception.BusinessException;
import com.vueadmin.repository.QualityRuleTemplateRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 质量规则模板Service
 */
@Slf4j
@Service
public class QualityRuleTemplateService {

    @Autowired
    private QualityRuleTemplateRepository templateRepository;

    /**
     * 获取模板列表
     */
    public List<QualityRuleTemplateDto> getTemplateList(String templateType, String status, String isSystem) {
        List<QualityRuleTemplate> templates;

        // 处理空字符串，转为 null
        templateType = (templateType != null && templateType.isEmpty()) ? null : templateType;
        status = (status != null && status.isEmpty()) ? null : status;
        isSystem = (isSystem != null && isSystem.isEmpty()) ? null : isSystem;

        // 处理 isSystem 参数
        final Boolean isSystemBool;
        if (isSystem != null) {
            isSystemBool = "true".equalsIgnoreCase(isSystem);
        } else {
            isSystemBool = null;
        }

        // 组合查询条件
        if (templateType != null && status != null && isSystemBool != null) {
            templates = templateRepository.findByTemplateTypeAndStatus(templateType, status);
            templates = templates.stream()
                    .filter(t -> t.getIsSystem().equals(isSystemBool))
                    .collect(Collectors.toList());
        } else if (templateType != null && isSystemBool != null) {
            templates = templateRepository.findByTemplateType(templateType);
            templates = templates.stream()
                    .filter(t -> t.getIsSystem().equals(isSystemBool))
                    .collect(Collectors.toList());
        } else if (status != null && isSystemBool != null) {
            templates = templateRepository.findByStatus(status);
            templates = templates.stream()
                    .filter(t -> t.getIsSystem().equals(isSystemBool))
                    .collect(Collectors.toList());
        } else if (isSystemBool != null) {
            templates = templateRepository.findByIsSystem(isSystemBool);
        } else if (templateType != null && status != null) {
            templates = templateRepository.findByTemplateTypeAndStatus(templateType, status);
        } else if (templateType != null) {
            templates = templateRepository.findByTemplateType(templateType);
        } else if (status != null) {
            templates = templateRepository.findByStatus(status);
        } else {
            templates = templateRepository.findAll();
        }

        return templates.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 获取模板详情
     */
    public QualityRuleTemplateDto getTemplateById(Long id) {
        QualityRuleTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new BusinessException("模板不存在"));
        return convertToDto(template);
    }

    /**
     * 创建模板
     */
    @Transactional
    public QualityRuleTemplateDto createTemplate(QualityRuleTemplateDto dto, String createdBy) {
        // 检查编码是否已存在
        QualityRuleTemplate existing = templateRepository.findByTemplateCode(dto.getTemplateCode());
        if (existing != null) {
            throw new BusinessException("模板编码已存在");
        }

        QualityRuleTemplate template = new QualityRuleTemplate();
        BeanUtils.copyProperties(dto, template);
        template.setCreatedBy(createdBy);

        template = templateRepository.save(template);
        return convertToDto(template);
    }

    /**
     * 更新模板
     */
    @Transactional
    public QualityRuleTemplateDto updateTemplate(Long id, QualityRuleTemplateDto dto, String updatedBy) {
        QualityRuleTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new BusinessException("模板不存在"));

        // 启用状态的模板不能编辑
        if ("active".equals(template.getStatus())) {
            throw new BusinessException("启用状态的模板不能编辑，请先重置为草稿");
        }

        // 检查编码是否被其他模板使用
        if (!template.getTemplateCode().equals(dto.getTemplateCode())) {
            QualityRuleTemplate existing = templateRepository.findByTemplateCode(dto.getTemplateCode());
            if (existing != null) {
                throw new BusinessException("模板编码已被使用");
            }
        }

        BeanUtils.copyProperties(dto, template);
        template.setUpdatedBy(updatedBy);

        template = templateRepository.save(template);
        return convertToDto(template);
    }

    /**
     * 删除模板
     */
    @Transactional
    public void deleteTemplate(Long id) {
        QualityRuleTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new BusinessException("模板不存在"));

        // 启用状态的模板不能删除
        if ("active".equals(template.getStatus())) {
            throw new BusinessException("启用状态的模板不能删除，请先重置为草稿");
        }

        // 检查是否被规则引用
        // TODO: 添加引用检查

        templateRepository.delete(template);
    }

    /**
     * 发布模板（草稿 -> 启用）
     */
    @Transactional
    public QualityRuleTemplateDto publishTemplate(Long id) {
        QualityRuleTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new BusinessException("模板不存在"));

        if ("active".equals(template.getStatus())) {
            throw new BusinessException("模板已处于启用状态");
        }

        template.setStatus("active");
        template = templateRepository.save(template);

        return convertToDto(template);
    }

    /**
     * 重置模板（启用 -> 草稿）
     */
    @Transactional
    public QualityRuleTemplateDto resetTemplate(Long id) {
        QualityRuleTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new BusinessException("模板不存在"));

        if (!"active".equals(template.getStatus())) {
            throw new BusinessException("只有启用状态的模板才能重置");
        }

        template.setStatus("inactive");
        template = templateRepository.save(template);

        return convertToDto(template);
    }

    /**
     * 复制模板（基于现有模板创建自定义模板）
     */
    @Transactional
    public QualityRuleTemplateDto copyTemplate(Long id, String newName, String createdBy) {
        QualityRuleTemplate source = templateRepository.findById(id)
                .orElseThrow(() -> new BusinessException("源模板不存在"));

        // 生成新的模板编码
        String newCode = source.getTemplateCode() + "_COPY_" + System.currentTimeMillis();

        // 检查新编码是否已存在
        QualityRuleTemplate existing = templateRepository.findByTemplateCode(newCode);
        if (existing != null) {
            newCode = source.getTemplateCode() + "_COPY_" + System.currentTimeMillis();
        }

        QualityRuleTemplate newTemplate = new QualityRuleTemplate();
        BeanUtils.copyProperties(source, newTemplate);
        newTemplate.setId(null);
        newTemplate.setTemplateCode(newCode);
        newTemplate.setTemplateName(newName != null ? newName : source.getTemplateName() + "（副本）");
        newTemplate.setIsSystem(false);
        newTemplate.setCreatedBy(createdBy);
        newTemplate.setUpdatedBy(createdBy);

        newTemplate = templateRepository.save(newTemplate);
        return convertToDto(newTemplate);
    }

    /**
     * 转换为DTO
     */
    private QualityRuleTemplateDto convertToDto(QualityRuleTemplate template) {
        QualityRuleTemplateDto dto = new QualityRuleTemplateDto();
        BeanUtils.copyProperties(template, dto);
        return dto;
    }
}

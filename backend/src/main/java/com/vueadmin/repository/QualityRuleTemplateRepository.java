package com.vueadmin.repository;

import com.vueadmin.entity.quality.QualityRuleTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 质量规则模板Repository
 */
@Repository
public interface QualityRuleTemplateRepository extends JpaRepository<QualityRuleTemplate, Long> {

    /**
     * 根据模板类型查询
     */
    List<QualityRuleTemplate> findByTemplateType(String templateType);

    /**
     * 根据状态查询
     */
    List<QualityRuleTemplate> findByStatus(String status);

    /**
     * 根据模板类型和状态查询
     */
    List<QualityRuleTemplate> findByTemplateTypeAndStatus(String templateType, String status);

    /**
     * 根据模板编码查询
     */
    QualityRuleTemplate findByTemplateCode(String templateCode);

    /**
     * 根据是否系统模板查询
     */
    List<QualityRuleTemplate> findByIsSystem(Boolean isSystem);

    /**
     * 根据是否系统模板和状态查询
     */
    List<QualityRuleTemplate> findByIsSystemAndStatus(Boolean isSystem, String status);
}

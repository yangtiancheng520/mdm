package com.vueadmin.repository;

import com.vueadmin.entity.standard.EncodingRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 编码规则Repository
 */
@Repository
public interface EncodingRuleRepository extends JpaRepository<EncodingRule, Long>, JpaSpecificationExecutor<EncodingRule> {

    /**
     * 根据规则编码查找
     */
    Optional<EncodingRule> findByRuleCode(String ruleCode);

    /**
     * 检查规则编码是否存在
     */
    boolean existsByRuleCode(String ruleCode);

    /**
     * 根据状态查找
     */
    java.util.List<EncodingRule> findByStatus(String status);
}

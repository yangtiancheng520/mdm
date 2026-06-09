package com.vueadmin.repository;

import com.vueadmin.entity.quality.QualityRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 质量规则Repository
 */
@Repository
public interface QualityRuleRepository extends JpaRepository<QualityRule, Long> {

    Optional<QualityRule> findByRuleCode(String ruleCode);

    List<QualityRule> findByViewId(Long viewId);

    List<QualityRule> findByEntityId(Long entityId);

    List<QualityRule> findByStatus(String status);

    List<QualityRule> findByViewIdAndStatus(Long viewId, String status);

    List<QualityRule> findByEntityIdAndStatus(Long entityId, String status);

    @Query("SELECT r FROM QualityRule r WHERE r.viewId = :viewId AND r.entityType = :entityType AND r.status = 'active'")
    List<QualityRule> findByViewIdAndEntityType(@Param("viewId") Long viewId, @Param("entityType") String entityType);

    @Query("SELECT r FROM QualityRule r WHERE r.entityId IN :entityIds AND r.status = 'active'")
    List<QualityRule> findByEntityIds(@Param("entityIds") List<Long> entityIds);

    @Query("SELECT r FROM QualityRule r WHERE r.viewId IN :viewIds AND r.status = :status")
    List<QualityRule> findByViewIdInAndStatus(@Param("viewIds") List<Long> viewIds, @Param("status") String status);

    boolean existsByRuleCode(String ruleCode);
}

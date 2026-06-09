package com.vueadmin.repository;

import com.vueadmin.entity.quality.QualityCheckResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QualityCheckResultRepository extends JpaRepository<QualityCheckResult, Long> {

    List<QualityCheckResult> findByTaskId(Long taskId);

    List<QualityCheckResult> findByTaskIdAndIsPassed(Long taskId, Integer isPassed);

    List<QualityCheckResult> findByRuleId(Long ruleId);

    @Query("SELECT r FROM QualityCheckResult r WHERE r.taskId = :taskId AND r.entityType = :entityType")
    List<QualityCheckResult> findByTaskIdAndEntityType(@Param("taskId") Long taskId, @Param("entityType") String entityType);

    @Query("SELECT COUNT(r) FROM QualityCheckResult r WHERE r.taskId = :taskId AND r.isPassed = 0")
    Long countFailedByTaskId(@Param("taskId") Long taskId);

    @Query("SELECT COUNT(r) FROM QualityCheckResult r WHERE r.taskId = :taskId AND r.isPassed = 1")
    Long countPassedByTaskId(@Param("taskId") Long taskId);

    void deleteByTaskId(Long taskId);
}
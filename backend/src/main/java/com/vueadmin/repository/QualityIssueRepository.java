package com.vueadmin.repository;

import com.vueadmin.entity.quality.QualityIssue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QualityIssueRepository extends JpaRepository<QualityIssue, Long> {

    Optional<QualityIssue> findByIssueCode(String issueCode);

    List<QualityIssue> findByTaskId(Long taskId);

    List<QualityIssue> findByStatus(String status);

    List<QualityIssue> findByAssignee(String assignee);

    List<QualityIssue> findByViewId(Long viewId);

    List<QualityIssue> findByIssueLevel(String issueLevel);

    @Query("SELECT i FROM QualityIssue i WHERE i.status IN ('open', 'processing')")
    List<QualityIssue> findOpenIssues();

    @Query("SELECT COUNT(i) FROM QualityIssue i WHERE i.status = :status")
    Long countByStatus(@Param("status") String status);

    @Query("SELECT i FROM QualityIssue i WHERE " +
           "(:status IS NULL OR i.status = :status) AND " +
           "(:issueLevel IS NULL OR i.issueLevel = :issueLevel) AND " +
           "(:viewId IS NULL OR i.viewId = :viewId)")
    List<QualityIssue> findByFilters(@Param("status") String status,
                                      @Param("issueLevel") String issueLevel,
                                      @Param("viewId") Long viewId);

    /**
     * 根据表名和记录ID查询问题列表
     */
    @Query("SELECT i FROM QualityIssue i WHERE i.tableName = :tableName AND (i.recordId = :recordId OR i.mainRecordId = :recordId)")
    List<QualityIssue> findByTableNameAndRecordId(@Param("tableName") String tableName, @Param("recordId") Long recordId);

    /**
     * 根据表名和记录ID查询未解决的问题
     */
    @Query("SELECT i FROM QualityIssue i WHERE i.tableName = :tableName AND (i.recordId = :recordId OR i.mainRecordId = :recordId) AND i.status IN ('open', 'processing')")
    List<QualityIssue> findOpenIssuesByTableNameAndRecordId(@Param("tableName") String tableName, @Param("recordId") Long recordId);

    /**
     * 按视图分组查询问题（用于分组显示）
     */
    @Query("SELECT DISTINCT i.viewId FROM QualityIssue i WHERE i.viewId IS NOT NULL AND i.status IN ('open', 'processing')")
    List<Long> findDistinctViewIdsWithOpenIssues();

    /**
     * 按视图和主记录ID分组查询问题
     */
    @Query("SELECT i FROM QualityIssue i WHERE i.viewId = :viewId AND i.status IN ('open', 'processing') ORDER BY i.mainRecordId, i.recordId")
    List<QualityIssue> findOpenIssuesByViewId(@Param("viewId") Long viewId);
}

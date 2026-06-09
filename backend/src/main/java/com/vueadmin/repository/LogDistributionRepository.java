package com.vueadmin.repository;

import com.vueadmin.entity.distribution.LogDistribution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 分发日志Repository
 */
@Repository
public interface LogDistributionRepository extends JpaRepository<LogDistribution, Long> {

    Optional<LogDistribution> findByLogCode(String logCode);

    List<LogDistribution> findByDataType(String dataType);

    List<LogDistribution> findByDataId(Long dataId);

    List<LogDistribution> findByDataTypeAndDataId(String dataType, Long dataId);

    List<LogDistribution> findByStatus(String status);

    List<LogDistribution> findBySystemConfigId(Long systemConfigId);

    Page<LogDistribution> findByDataType(String dataType, Pageable pageable);

    Page<LogDistribution> findByStatus(String status, Pageable pageable);

    @Query("SELECT l FROM LogDistribution l WHERE l.dataType = :dataType AND l.dataId = :dataId ORDER BY l.createdAt DESC")
    List<LogDistribution> findByDataTypeAndDataIdOrderByCreatedDesc(@Param("dataType") String dataType, @Param("dataId") Long dataId);

    @Query("SELECT l FROM LogDistribution l WHERE l.dataType = :dataType AND l.status = :status ORDER BY l.createdAt DESC")
    List<LogDistribution> findByDataTypeAndStatusOrderByCreatedDesc(@Param("dataType") String dataType, @Param("status") String status);

    @Query("SELECT l FROM LogDistribution l WHERE " +
           "(:dataType IS NULL OR l.dataType = :dataType) AND " +
           "(:status IS NULL OR l.status = :status) AND " +
           "(:systemConfigId IS NULL OR l.systemConfigId = :systemConfigId) AND " +
           "(:startTime IS NULL OR l.createdAt >= :startTime) AND " +
           "(:endTime IS NULL OR l.createdAt <= :endTime)")
    Page<LogDistribution> searchLogs(@Param("dataType") String dataType,
                                      @Param("status") String status,
                                      @Param("systemConfigId") Long systemConfigId,
                                      @Param("startTime") LocalDateTime startTime,
                                      @Param("endTime") LocalDateTime endTime,
                                      Pageable pageable);

    @Query("SELECT COUNT(l) FROM LogDistribution l WHERE l.dataType = :dataType AND l.status = :status")
    long countByDataTypeAndStatus(@Param("dataType") String dataType, @Param("status") String status);

    @Query("SELECT COUNT(l) FROM LogDistribution l WHERE l.createdAt BETWEEN :startTime AND :endTime")
    long countByCreatedBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Query("SELECT l.status, COUNT(l) FROM LogDistribution l WHERE l.createdAt BETWEEN :startTime AND :endTime GROUP BY l.status")
    List<Object[]> countByStatusAndCreatedBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    // ==================== 血缘数据修复相关方法 ====================

    /**
     * 统计指定状态的日志数量
     */
    @Query("SELECT COUNT(l) FROM LogDistribution l WHERE l.status = :status")
    long countByStatus(@Param("status") String status);

    /**
     * 查找成功的分发日志但没有血缘数据的
     */
    @Query("SELECT l FROM LogDistribution l WHERE l.status = 'SUCCESS' AND (l.fieldCount IS NULL OR l.fieldCount = 0) ORDER BY l.createdAt DESC")
    List<LogDistribution> findSuccessLogsWithoutLineage();

    /**
     * 查找所有没有血缘数据的分发日志（包括失败的）
     */
    @Query("SELECT l FROM LogDistribution l WHERE (l.fieldCount IS NULL OR l.fieldCount = 0) ORDER BY l.createdAt DESC")
    List<LogDistribution> findLogsWithoutLineage();

    /**
     * 查找指定数据类型的所有没有血缘数据的分发日志
     */
    @Query("SELECT l FROM LogDistribution l WHERE l.dataType = :dataType AND (l.fieldCount IS NULL OR l.fieldCount = 0) ORDER BY l.createdAt DESC")
    List<LogDistribution> findLogsWithoutLineageByType(@Param("dataType") String dataType);

    /**
     * 统计有血缘数据的日志数量
     */
    @Query("SELECT COUNT(DISTINCT l.id) FROM LogDistribution l WHERE l.fieldCount IS NOT NULL AND l.fieldCount > 0")
    long countLogsWithLineage();

    /**
     * 统计需要补充血缘的成功日志数量
     */
    @Query("SELECT COUNT(l) FROM LogDistribution l WHERE l.status = 'SUCCESS' AND (l.fieldCount IS NULL OR l.fieldCount = 0)")
    long countSuccessLogsWithoutLineage();
}

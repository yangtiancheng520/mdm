package com.vueadmin.repository;

import com.vueadmin.entity.distribution.DistributionBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 分发批次Repository
 */
@Repository
public interface DistributionBatchRepository extends JpaRepository<DistributionBatch, Long> {

    Optional<DistributionBatch> findByBatchCode(String batchCode);

    List<DistributionBatch> findByDataType(String dataType);

    List<DistributionBatch> findByStatus(String status);

    List<DistributionBatch> findBySystemConfigId(Long systemConfigId);

    @Query("SELECT b FROM DistributionBatch b WHERE b.createdAt BETWEEN :startTime AND :endTime ORDER BY b.createdAt DESC")
    List<DistributionBatch> findByCreatedBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Query("SELECT b FROM DistributionBatch b WHERE b.status IN ('pending', 'running') ORDER BY b.createdAt")
    List<DistributionBatch> findPendingBatches();

    @Query("SELECT COUNT(b) FROM DistributionBatch b WHERE b.status = :status")
    long countByStatus(@Param("status") String status);
}

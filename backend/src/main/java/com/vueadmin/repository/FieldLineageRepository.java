package com.vueadmin.repository;

import com.vueadmin.entity.distribution.FieldLineage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 字段级血缘Repository
 */
@Repository
public interface FieldLineageRepository extends JpaRepository<FieldLineage, Long> {

    List<FieldLineage> findByLogId(Long logId);

    List<FieldLineage> findByDataTypeAndDataId(String dataType, Long dataId);

    @Query("SELECT fl FROM FieldLineage fl WHERE fl.dataType = :dataType AND fl.dataId = :dataId ORDER BY fl.createdAt DESC")
    List<FieldLineage> findByDataOrderByTime(@Param("dataType") String dataType, @Param("dataId") Long dataId);

    @Query("SELECT fl FROM FieldLineage fl WHERE fl.logId = :logId ORDER BY fl.id")
    List<FieldLineage> findByLogIdOrderById(@Param("logId") Long logId);

    @Query("SELECT COUNT(fl) FROM FieldLineage fl WHERE fl.logId = :logId AND fl.status = :status")
    long countByLogIdAndStatus(@Param("logId") Long logId, @Param("status") String status);
}

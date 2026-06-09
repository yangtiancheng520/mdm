package com.vueadmin.repository;

import com.vueadmin.entity.distribution.FieldMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 字段映射Repository
 */
@Repository
public interface FieldMappingRepository extends JpaRepository<FieldMapping, Long> {

    List<FieldMapping> findByDataType(String dataType);

    List<FieldMapping> findByDataTypeAndStatus(String dataType, String status);

    List<FieldMapping> findBySystemConfigId(Long systemConfigId);

    List<FieldMapping> findByDataTypeAndSystemConfigId(String dataType, Long systemConfigId);

    @Query("SELECT m FROM FieldMapping m WHERE m.dataType = :dataType AND m.status = 'active' ORDER BY m.sortOrder")
    List<FieldMapping> findActiveByDataType(@Param("dataType") String dataType);

    @Query("SELECT m FROM FieldMapping m WHERE m.dataType = :dataType AND m.systemConfigId = :systemConfigId AND m.status = 'active' ORDER BY m.sortOrder")
    List<FieldMapping> findActiveByDataTypeAndSystemConfig(@Param("dataType") String dataType, @Param("systemConfigId") Long systemConfigId);

    void deleteByDataType(String dataType);

    void deleteByDataTypeAndSystemConfigId(String dataType, Long systemConfigId);
}

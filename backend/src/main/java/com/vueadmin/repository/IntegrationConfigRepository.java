package com.vueadmin.repository;

import com.vueadmin.entity.system.IntegrationConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 外部集成配置Repository
 */
@Repository
public interface IntegrationConfigRepository extends JpaRepository<IntegrationConfig, Long> {

    Optional<IntegrationConfig> findByIntegrationCode(String integrationCode);

    List<IntegrationConfig> findByStatus(String status);

    List<IntegrationConfig> findByIntegrationType(String integrationType);

    @Query("SELECT ic FROM IntegrationConfig ic WHERE " +
           "(:integrationCode IS NULL OR ic.integrationCode LIKE %:integrationCode%) AND " +
           "(:integrationName IS NULL OR ic.integrationName LIKE %:integrationName%) AND " +
           "(:integrationType IS NULL OR ic.integrationType = :integrationType) AND " +
           "(:status IS NULL OR ic.status = :status)")
    List<IntegrationConfig> searchConfigs(@Param("integrationCode") String integrationCode,
                                          @Param("integrationName") String integrationName,
                                          @Param("integrationType") String integrationType,
                                          @Param("status") String status);

    boolean existsByIntegrationCode(String integrationCode);
}

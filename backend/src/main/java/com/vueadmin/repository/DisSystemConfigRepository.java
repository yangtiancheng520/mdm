package com.vueadmin.repository;

import com.vueadmin.entity.distribution.DisSystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 分发系统配置Repository
 */
@Repository
public interface DisSystemConfigRepository extends JpaRepository<DisSystemConfig, Long> {

    Optional<DisSystemConfig> findByConfigCode(String configCode);

    List<DisSystemConfig> findBySystemType(String systemType);

    List<DisSystemConfig> findByStatus(String status);

    @Query("SELECT c FROM DisSystemConfig c WHERE c.systemType = :systemType AND c.status = 'active'")
    List<DisSystemConfig> findActiveBySystemType(@Param("systemType") String systemType);

    @Query("SELECT c FROM DisSystemConfig c WHERE c.isDefault = 1 AND c.status = 'active'")
    Optional<DisSystemConfig> findDefaultConfig();

    @Query("SELECT c FROM DisSystemConfig c WHERE c.systemType = :systemType AND c.isDefault = 1 AND c.status = 'active'")
    Optional<DisSystemConfig> findDefaultBySystemType(@Param("systemType") String systemType);

    boolean existsByConfigCode(String configCode);
}

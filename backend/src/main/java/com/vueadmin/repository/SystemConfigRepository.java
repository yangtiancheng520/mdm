package com.vueadmin.repository;

import com.vueadmin.entity.system.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 系统配置Repository
 */
@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfig, Long> {

    Optional<SystemConfig> findByConfigKey(String configKey);

    List<SystemConfig> findByConfigGroup(String configGroup);

    @Query("SELECT sc FROM SystemConfig sc WHERE " +
           "(:configKey IS NULL OR sc.configKey LIKE %:configKey%) AND " +
           "(:configGroup IS NULL OR sc.configGroup = :configGroup)")
    List<SystemConfig> searchConfigs(@Param("configKey") String configKey,
                                     @Param("configGroup") String configGroup);

    boolean existsByConfigKey(String configKey);

    void deleteByConfigKey(String configKey);
}

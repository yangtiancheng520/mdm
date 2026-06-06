package com.vueadmin.repository;

import com.vueadmin.entity.system.RuleScript;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 规则脚本Repository
 */
@Repository
public interface RuleScriptRepository extends JpaRepository<RuleScript, Long> {

    Optional<RuleScript> findByScriptCode(String scriptCode);

    List<RuleScript> findByStatus(String status);

    @Query("SELECT rs FROM RuleScript rs WHERE " +
           "(:scriptCode IS NULL OR rs.scriptCode LIKE %:scriptCode%) AND " +
           "(:scriptName IS NULL OR rs.scriptName LIKE %:scriptName%) AND " +
           "(:scriptType IS NULL OR rs.scriptType = :scriptType) AND " +
           "(:status IS NULL OR rs.status = :status)")
    List<RuleScript> searchScripts(@Param("scriptCode") String scriptCode,
                                   @Param("scriptName") String scriptName,
                                   @Param("scriptType") String scriptType,
                                   @Param("status") String status);

    boolean existsByScriptCode(String scriptCode);
}

package com.vueadmin.repository;

import com.vueadmin.entity.system.DataPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 数据权限Repository
 */
@Repository
public interface DataPermissionRepository extends JpaRepository<DataPermission, Long> {

    List<DataPermission> findByRoleId(Long roleId);

    Optional<DataPermission> findByRoleIdAndDataType(Long roleId, String dataType);

    void deleteByRoleId(Long roleId);

    void deleteByRoleIdAndDataType(Long roleId, String dataType);

    @Query("SELECT dp FROM DataPermission dp WHERE dp.roleId IN :roleIds AND dp.dataType = :dataType")
    List<DataPermission> findByRoleIdsAndDataType(@Param("roleIds") List<Long> roleIds, @Param("dataType") String dataType);
}

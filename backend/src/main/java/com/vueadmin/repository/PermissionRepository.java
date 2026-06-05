package com.vueadmin.repository;

import com.vueadmin.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface PermissionRepository extends JpaRepository<Permission, Long> {

    @Query("SELECT p FROM Permission p WHERE :name IS NULL OR p.name LIKE CONCAT('%', :name, '%')")
    List<Permission> searchPermissions(@Param("name") String name);
}

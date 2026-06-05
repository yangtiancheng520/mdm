package com.vueadmin.repository;

import com.vueadmin.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface RoleRepository extends JpaRepository<Role, Long> {

    @Query("SELECT r FROM Role r WHERE :name IS NULL OR r.name LIKE CONCAT('%', :name, '%')")
    List<Role> searchRoles(@Param("name") String name);
}

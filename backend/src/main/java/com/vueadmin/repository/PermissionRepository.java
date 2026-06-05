package com.vueadmin.repository;

import com.vueadmin.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface PermissionRepository extends JpaRepository<Permission, Long> {

    @Query("SELECT p FROM Permission p WHERE :name IS NULL OR p.name LIKE CONCAT('%', :name, '%')")
    List<Permission> searchPermissions(@Param("name") String name);

    /**
     * 按类型查询并按排序
     */
    List<Permission> findByTypeOrderBySortAsc(Permission.Type type);

    /**
     * 按类型和状态查询
     */
    List<Permission> findByTypeAndStatusOrderBySortAsc(Permission.Type type, Permission.Status status);

    /**
     * 按父ID查询
     */
    List<Permission> findByParentIdOrderBySortAsc(Long parentId);

    /**
     * 查询所有菜单（包括按钮）
     */
    @Query("SELECT p FROM Permission p ORDER BY p.sort ASC")
    List<Permission> findAllOrderBySortAsc();
}

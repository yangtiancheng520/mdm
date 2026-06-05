package com.vueadmin.repository;

import com.vueadmin.entity.system.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 组织机构Repository
 */
@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    /**
     * 根据组织编码查询
     */
    Optional<Organization> findByOrgCode(String orgCode);

    /**
     * 检查组织编码是否存在
     */
    boolean existsByOrgCode(String orgCode);

    /**
     * 查询所有启用的组织
     */
    List<Organization> findByStatusOrderBySortAsc(String status);

    /**
     * 查询所有组织（按排序）
     */
    List<Organization> findAllByOrderBySortAsc();

    /**
     * 根据父ID查询子组织
     */
    List<Organization> findByParentIdOrderBySortAsc(Long parentId);

    /**
     * 根据父ID和状态查询子组织
     */
    List<Organization> findByParentIdAndStatusOrderBySortAsc(Long parentId, String status);

    /**
     * 查询顶级组织（parentId为null）
     */
    @Query("SELECT o FROM Organization o WHERE o.parentId IS NULL ORDER BY o.sort ASC")
    List<Organization> findRootOrganizations();

    /**
     * 查询顶级启用的组织
     */
    @Query("SELECT o FROM Organization o WHERE o.parentId IS NULL AND o.status = 'active' ORDER BY o.sort ASC")
    List<Organization> findActiveRootOrganizations();

    /**
     * 根据路径前缀查询（用于查询所有子组织）
     */
    @Query("SELECT o FROM Organization o WHERE o.path LIKE :pathPrefix ORDER BY o.sort ASC")
    List<Organization> findByPathStartingWith(@Param("pathPrefix") String pathPrefix);

    /**
     * 统计子组织数量
     */
    long countByParentId(Long parentId);

    /**
     * 根据名称模糊查询
     */
    List<Organization> findByOrgNameContainingOrderBySortAsc(String orgName);
}

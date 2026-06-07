package com.vueadmin.repository;

import com.vueadmin.entity.standard.ViewCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 视图分类Repository
 */
@Repository
public interface ViewCategoryRepository extends JpaRepository<ViewCategory, Long> {

    /**
     * 根据分类编码查询
     */
    Optional<ViewCategory> findByCategoryCode(String categoryCode);

    /**
     * 检查分类编码是否存在
     */
    boolean existsByCategoryCode(String categoryCode);

    /**
     * 检查分类编码是否存在（排除指定ID）
     */
    boolean existsByCategoryCodeAndIdNot(String categoryCode, Long id);

    /**
     * 查询所有分类（按排序字段排序）
     */
    List<ViewCategory> findAllByOrderBySortAsc();

    /**
     * 根据父级ID查询子分类
     */
    List<ViewCategory> findByParentIdOrderBySortAsc(Long parentId);

    /**
     * 查询顶级分类（parentId为null）
     */
    List<ViewCategory> findByParentIdIsNullOrderBySortAsc();

    /**
     * 根据状态查询分类
     */
    List<ViewCategory> findByStatusOrderBySortAsc(String status);

    /**
     * 检查是否有子分类
     */
    boolean existsByParentId(Long parentId);

    /**
     * 统计分类下的视图数量（只统计主干版本）
     */
    @Query("SELECT COUNT(v) FROM ViewDefinition v WHERE v.categoryId = :categoryId AND (v.isTrunk = true OR v.status = 'draft')")
    long countViewsByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 查询所有启用的分类
     */
    @Query("SELECT vc FROM ViewCategory vc WHERE vc.status = 'active' ORDER BY vc.sort ASC")
    List<ViewCategory> findAllActive();
}

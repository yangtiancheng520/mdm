package com.vueadmin.repository;

import com.vueadmin.entity.standard.FieldCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 字段分类Repository
 */
@Repository
public interface FieldCategoryRepository extends JpaRepository<FieldCategory, Long> {

    /**
     * 查询所有分类（按排序字段排序）
     */
    List<FieldCategory> findAllByOrderBySortAsc();

    /**
     * 根据父级ID查询子分类
     */
    List<FieldCategory> findByParentIdOrderBySortAsc(Long parentId);

    /**
     * 查询顶级分类（parentId为null）
     */
    List<FieldCategory> findByParentIdIsNullOrderBySortAsc();

    /**
     * 根据状态查询分类
     */
    List<FieldCategory> findByStatusOrderBySortAsc(String status);

    /**
     * 检查分类编码是否存在
     */
    boolean existsByCategoryCode(String categoryCode);

    /**
     * 检查分类编码是否存在（排除指定ID）
     */
    boolean existsByCategoryCodeAndIdNot(String categoryCode, Long id);

    /**
     * 检查是否有子分类
     */
    boolean existsByParentId(Long parentId);

    /**
     * 统计分类下的字段数量
     */
    @Query("SELECT COUNT(fs) FROM FieldStandard fs WHERE fs.categoryId = :categoryId")
    long countFieldsByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 查询所有启用的分类
     */
    @Query("SELECT fc FROM FieldCategory fc WHERE fc.status = 'active' ORDER BY fc.sort ASC")
    List<FieldCategory> findAllActive();
}

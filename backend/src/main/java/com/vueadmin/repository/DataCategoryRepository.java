package com.vueadmin.repository;

import com.vueadmin.entity.data.DataCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 数据分类Repository
 */
@Repository
public interface DataCategoryRepository extends JpaRepository<DataCategory, Long> {

    /**
     * 查询所有分类（按排序）
     */
    @Query("SELECT c FROM DataCategory c ORDER BY c.sort ASC, c.id ASC")
    List<DataCategory> findAllOrderBySort();

    /**
     * 根据父级ID查询子分类
     */
    @Query("SELECT c FROM DataCategory c WHERE c.parentId = ?1 ORDER BY c.sort ASC, c.id ASC")
    List<DataCategory> findByParentId(Long parentId);

    /**
     * 查询顶级分类
     */
    @Query("SELECT c FROM DataCategory c WHERE c.parentId IS NULL ORDER BY c.sort ASC, c.id ASC")
    List<DataCategory> findRootCategories();

    /**
     * 查询文件夹下的子项
     */
    @Query("SELECT c FROM DataCategory c WHERE c.parentId = ?1 ORDER BY c.sort ASC, c.id ASC")
    List<DataCategory> findByFolderId(Long folderId);
}

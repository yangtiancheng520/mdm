package com.vueadmin.repository;

import com.vueadmin.entity.data.DataInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 数据实例Repository
 */
@Repository
public interface DataInstanceRepository extends JpaRepository<DataInstance, Long> {

    /**
     * 根据分类ID查询数据列表
     */
    @Query("SELECT i FROM DataInstance i WHERE i.categoryId = ?1 AND i.status = 'active' ORDER BY i.createdAt DESC")
    List<DataInstance> findByCategoryId(Long categoryId);

    /**
     * 根据表单ID查询数据列表
     */
    @Query("SELECT i FROM DataInstance i WHERE i.formId = ?1 AND i.status = 'active' ORDER BY i.createdAt DESC")
    List<DataInstance> findByFormId(Long formId);

    /**
     * 根据分类ID和表单ID查询数据列表
     */
    @Query("SELECT i FROM DataInstance i WHERE i.categoryId = ?1 AND i.formId = ?2 AND i.status = 'active' ORDER BY i.createdAt DESC")
    List<DataInstance> findByCategoryIdAndFormId(Long categoryId, Long formId);
}
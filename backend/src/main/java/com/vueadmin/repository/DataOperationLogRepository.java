package com.vueadmin.repository;

import com.vueadmin.entity.data.DataOperationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 数据操作日志Repository
 */
@Repository
public interface DataOperationLogRepository extends JpaRepository<DataOperationLog, Long> {

    /**
     * 根据分类ID查询日志列表
     */
    @Query("SELECT l FROM DataOperationLog l WHERE l.categoryId = ?1 ORDER BY l.createdAt DESC")
    List<DataOperationLog> findByCategoryId(Long categoryId);

    /**
     * 根据表单ID查询日志列表
     */
    @Query("SELECT l FROM DataOperationLog l WHERE l.formId = ?1 ORDER BY l.createdAt DESC")
    List<DataOperationLog> findByFormId(Long formId);

    /**
     * 根据分类ID和表单ID查询日志列表
     */
    @Query("SELECT l FROM DataOperationLog l WHERE l.categoryId = ?1 AND l.formId = ?2 ORDER BY l.createdAt DESC")
    List<DataOperationLog> findByCategoryIdAndFormId(Long categoryId, Long formId);

    /**
     * 根据主表记录ID查询日志列表
     */
    @Query("SELECT l FROM DataOperationLog l WHERE l.mainRecordId = ?1 ORDER BY l.createdAt DESC")
    List<DataOperationLog> findByMainRecordId(Long mainRecordId);

    /**
     * 根据表单ID和记录ID查询日志列表
     */
    @Query("SELECT l FROM DataOperationLog l WHERE l.formId = ?1 AND l.mainRecordId = ?2 ORDER BY l.createdAt DESC")
    List<DataOperationLog> findByFormIdAndMainRecordIdOrderByCreatedAtDesc(Long formId, Long mainRecordId);

    /**
     * 根据操作类型查询日志列表
     */
    @Query("SELECT l FROM DataOperationLog l WHERE l.operationType = ?1 ORDER BY l.createdAt DESC")
    List<DataOperationLog> findByOperationType(String operationType);
}

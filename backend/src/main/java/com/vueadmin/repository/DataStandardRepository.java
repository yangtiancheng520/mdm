package com.vueadmin.repository;

import com.vueadmin.entity.standard.DataStandard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 数据标准视图Repository
 */
@Repository
public interface DataStandardRepository extends JpaRepository<DataStandard, Long> {

    /**
     * 根据标准编码查询
     */
    Optional<DataStandard> findByStandardCode(String standardCode);

    /**
     * 检查标准编码是否存在
     */
    boolean existsByStandardCode(String standardCode);

    /**
     * 根据状态查询
     */
    List<DataStandard> findByStatus(String status);

    /**
     * 根据状态分页查询
     */
    Page<DataStandard> findByStatus(String status, Pageable pageable);

    /**
     * 根据分类ID查询
     */
    List<DataStandard> findByCategoryId(Long categoryId);

    /**
     * 统计各状态数量
     */
    long countByStatus(String status);

    /**
     * 多条件查询
     */
    @Query("SELECT ds FROM DataStandard ds WHERE " +
           "(:keyword IS NULL OR ds.standardCode LIKE %:keyword% OR ds.standardName LIKE %:keyword%) AND " +
           "(:status IS NULL OR ds.status = :status) AND " +
           "(:categoryId IS NULL OR ds.categoryId = :categoryId)")
    Page<DataStandard> search(
            @Param("keyword") String keyword,
            @Param("status") String status,
            @Param("categoryId") Long categoryId,
            Pageable pageable);
}

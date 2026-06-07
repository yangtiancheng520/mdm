package com.vueadmin.repository;

import com.vueadmin.entity.standard.FieldStandard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 字段标准Repository
 * 提供字段标准的数据访问操作
 */
@Repository
public interface FieldStandardRepository extends JpaRepository<FieldStandard, Long> {

    /**
     * 根据字段编码查询
     *
     * @param fieldCode 字段编码
     * @return 字段标准对象
     */
    Optional<FieldStandard> findByFieldCode(String fieldCode);

    /**
     * 根据状态查询列表
     *
     * @param status 状态
     * @return 字段标准列表
     */
    List<FieldStandard> findByStatus(String status);

    /**
     * 根据分类查询列表（兼容旧字段）
     *
     * @param category 分类
     * @return 字段标准列表
     */
    List<FieldStandard> findByCategory(String category);

    /**
     * 根据分类ID查询列表
     *
     * @param categoryId 分类ID
     * @return 字段标准列表
     */
    List<FieldStandard> findByCategoryId(Long categoryId);

    /**
     * 检查字段编码是否存在
     *
     * @param fieldCode 字段编码
     * @return 是否存在
     */
    boolean existsByFieldCode(String fieldCode);

    /**
     * 根据字段编码删除
     *
     * @param fieldCode 字段编码
     */
    void deleteByFieldCode(String fieldCode);

    /**
     * 根据状态和分类ID查询列表
     *
     * @param status     状态
     * @param categoryId 分类ID
     * @return 字段标准列表
     */
    List<FieldStandard> findByStatusAndCategoryId(String status, Long categoryId);

    /**
     * 分页查询字段标准（支持多条件筛选）
     *
     * @param fieldCode  字段编码（模糊查询）
     * @param fieldName  字段名称（模糊查询）
     * @param fieldType  字段类型
     * @param status     状态
     * @param category   分类（旧字段，兼容）
     * @param categoryId 分类ID
     * @param pageable   分页参数
     * @return 分页结果
     */
    @Query("SELECT fs FROM FieldStandard fs WHERE " +
           "(:fieldCode IS NULL OR fs.fieldCode LIKE %:fieldCode%) AND " +
           "(:fieldName IS NULL OR fs.fieldName LIKE %:fieldName%) AND " +
           "(:fieldType IS NULL OR fs.fieldType = :fieldType) AND " +
           "(:status IS NULL OR fs.status = :status) AND " +
           "(:category IS NULL OR fs.category = :category) AND " +
           "(:categoryId IS NULL OR fs.categoryId = :categoryId)")
    Page<FieldStandard> searchFieldStandards(
            @Param("fieldCode") String fieldCode,
            @Param("fieldName") String fieldName,
            @Param("fieldType") String fieldType,
            @Param("status") String status,
            @Param("category") String category,
            @Param("categoryId") Long categoryId,
            Pageable pageable);

    /**
     * 查询所有分类
     *
     * @return 分类列表
     */
    @Query("SELECT DISTINCT fs.category FROM FieldStandard fs WHERE fs.category IS NOT NULL")
    List<String> findAllCategories();

    /**
     * 根据值域ID查询
     *
     * @param domainId 值域ID
     * @return 字段标准列表
     */
    List<FieldStandard> findByDomainId(Long domainId);

    /**
     * 查询已启用的字段标准
     *
     * @return 已启用的字段标准列表
     */
    @Query("SELECT fs FROM FieldStandard fs WHERE fs.status = '启用' ORDER BY fs.fieldCode")
    List<FieldStandard> findAllActive();

    /**
     * 查询指定版本的字段标准
     *
     * @param fieldCode 字段编码
     * @param version   版本号
     * @return 字段标准对象
     */
    @Query("SELECT fs FROM FieldStandard fs WHERE fs.fieldCode = :fieldCode AND fs.version = :version")
    Optional<FieldStandard> findByFieldCodeAndVersion(
            @Param("fieldCode") String fieldCode,
            @Param("version") Integer version);

    /**
     * 统计指定状态的字段数量
     *
     * @param status 状态
     * @return 数量
     */
    long countByStatus(String status);

    /**
     * 统计指定分类的字段数量
     *
     * @param category 分类
     * @return 数量
     */
    long countByCategory(String category);
}

package com.vueadmin.repository;

import com.vueadmin.entity.standard.ValueDomain;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 值域Repository
 * 提供值域的数据访问操作
 */
@Repository
public interface ValueDomainRepository extends JpaRepository<ValueDomain, Long> {

    /**
     * 根据值域编码查询
     *
     * @param domainCode 值域编码
     * @return 值域对象
     */
    Optional<ValueDomain> findByDomainCode(String domainCode);

    /**
     * 根据状态查询列表
     *
     * @param status 状态
     * @return 值域列表
     */
    List<ValueDomain> findByStatus(String status);

    /**
     * 检查值域编码是否存在
     *
     * @param domainCode 值域编码
     * @return 是否存在
     */
    boolean existsByDomainCode(String domainCode);

    /**
     * 根据值域编码删除
     *
     * @param domainCode 值域编码
     */
    void deleteByDomainCode(String domainCode);

    /**
     * 分页查询值域（支持多条件筛选）
     *
     * @param domainCode 值域编码（模糊查询）
     * @param domainName 值域名称（模糊查询）
     * @param dataType   数据类型
     * @param status     状态
     * @param pageable   分页参数
     * @return 分页结果
     */
    @Query("SELECT vd FROM ValueDomain vd WHERE " +
           "(:domainCode IS NULL OR vd.domainCode LIKE %:domainCode%) AND " +
           "(:domainName IS NULL OR vd.domainName LIKE %:domainName%) AND " +
           "(:dataType IS NULL OR vd.dataType = :dataType) AND " +
           "(:status IS NULL OR vd.status = :status)")
    Page<ValueDomain> searchValueDomains(
            @Param("domainCode") String domainCode,
            @Param("domainName") String domainName,
            @Param("dataType") String dataType,
            @Param("status") String status,
            Pageable pageable);

    /**
     * 查询所有启用的值域
     *
     * @return 启用的值域列表
     */
    @Query("SELECT vd FROM ValueDomain vd WHERE vd.status = '启用' ORDER BY vd.domainCode")
    List<ValueDomain> findAllActive();

    /**
     * 根据数据类型查询启用的值域
     *
     * @param dataType 数据类型
     * @return 值域列表
     */
    List<ValueDomain> findByDataTypeAndStatus(String dataType, String status);

    /**
     * 统计指定状态的值域数量
     *
     * @param status 状态
     * @return 数量
     */
    long countByStatus(String status);
}

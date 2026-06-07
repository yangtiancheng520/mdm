package com.vueadmin.repository;

import com.vueadmin.entity.standard.ValueDomainItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 值域项Repository
 * 提供值域项的数据访问操作
 */
@Repository
public interface ValueDomainItemRepository extends JpaRepository<ValueDomainItem, Long> {

    /**
     * 根据值域ID查询所有项
     *
     * @param domainId 值域ID
     * @return 值域项列表
     */
    List<ValueDomainItem> findByDomainIdOrderBySortAsc(Long domainId);

    /**
     * 根据值域ID和状态查询项
     *
     * @param domainId 值域ID
     * @param status   状态
     * @return 值域项列表
     */
    List<ValueDomainItem> findByDomainIdAndStatusOrderBySortAsc(Long domainId, String status);

    /**
     * 根据值域ID删除所有项
     *
     * @param domainId 值域ID
     */
    @Modifying
    @Query("DELETE FROM ValueDomainItem vdi WHERE vdi.domainId = :domainId")
    void deleteByDomainId(@Param("domainId") Long domainId);

    /**
     * 根据值域ID统计项数量
     *
     * @param domainId 值域ID
     * @return 数量
     */
    long countByDomainId(Long domainId);

    /**
     * 检查项值是否存在
     *
     * @param domainId  值域ID
     * @param itemValue 项值
     * @return 是否存在
     */
    boolean existsByDomainIdAndItemValue(Long domainId, String itemValue);
}

package com.vueadmin.repository;

import com.vueadmin.entity.standard.ViewDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ViewDefinitionRepository extends JpaRepository<ViewDefinition, Long> {

    // 根据视图编码查询
    Optional<ViewDefinition> findByViewCode(String viewCode);

    // 根据视图编码和版本查询
    Optional<ViewDefinition> findByViewCodeAndVersion(String viewCode, Integer version);

    // 查询最新版本
    Optional<ViewDefinition> findByViewCodeAndIsLatestTrue(String viewCode);

    // 查询已发布版本
    Optional<ViewDefinition> findByViewCodeAndStatus(String viewCode, String status);

    // 查询某个视图编码的所有已发布版本
    List<ViewDefinition> findAllByViewCodeAndStatus(String viewCode, String status);

    // 查询某个视图编码的所有版本
    List<ViewDefinition> findByViewCodeOrderByVersionDesc(String viewCode);

    // 根据分类查询
    List<ViewDefinition> findByCategoryId(Long categoryId);

    // 根据状态查询
    List<ViewDefinition> findByStatus(String status);

    // 查询最新已发布版本
    @Query("SELECT v FROM ViewDefinition v WHERE v.viewCode = :viewCode AND v.status = 'published' AND v.isLatest = true")
    Optional<ViewDefinition> findLatestPublishedByViewCode(@Param("viewCode") String viewCode);

    // 检查视图编码是否存在
    boolean existsByViewCode(String viewCode);
}

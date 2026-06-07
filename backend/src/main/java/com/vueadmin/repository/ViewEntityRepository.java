package com.vueadmin.repository;

import com.vueadmin.entity.standard.ViewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ViewEntityRepository extends JpaRepository<ViewEntity, Long> {

    // 根据视图ID查询所有实体
    List<ViewEntity> findByViewId(Long viewId);

    // 根据视图ID查询所有实体（按排序）
    List<ViewEntity> findByViewIdOrderBySort(Long viewId);

    // 根据视图ID查询主实体
    Optional<ViewEntity> findByViewIdAndEntityType(Long viewId, String entityType);

    // 根据视图ID查询子实体
    List<ViewEntity> findByViewIdAndEntityTypeOrderBySort(Long viewId, String entityType);

    // 检查实体编码是否存在
    boolean existsByViewIdAndEntityCode(Long viewId, String entityCode);

    // 删除视图的所有实体
    void deleteByViewId(Long viewId);
}

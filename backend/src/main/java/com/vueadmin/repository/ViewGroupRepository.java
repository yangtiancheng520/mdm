package com.vueadmin.repository;

import com.vueadmin.entity.standard.ViewGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ViewGroupRepository extends JpaRepository<ViewGroup, Long> {

    // 根据实体ID查询所有分组
    List<ViewGroup> findByEntityIdOrderBySort(Long entityId);

    // 删除实体的所有分组
    void deleteByEntityId(Long entityId);
}

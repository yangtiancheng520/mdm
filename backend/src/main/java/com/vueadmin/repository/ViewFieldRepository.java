package com.vueadmin.repository;

import com.vueadmin.entity.standard.ViewField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ViewFieldRepository extends JpaRepository<ViewField, Long> {

    // 根据实体ID查询所有字段
    List<ViewField> findByEntityIdOrderBySort(Long entityId);

    // 根据分组ID查询字段
    List<ViewField> findByGroupIdOrderBySort(Long groupId);

    // 查询无分组的字段
    List<ViewField> findByEntityIdAndGroupIdIsNullOrderBySort(Long entityId);

    // 检查字段编码是否存在
    boolean existsByEntityIdAndFieldCode(Long entityId, String fieldCode);

    // 删除实体的所有字段
    void deleteByEntityId(Long entityId);
}

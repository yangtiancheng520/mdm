package com.vueadmin.repository;

import com.vueadmin.entity.standard.ViewValidation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ViewValidationRepository extends JpaRepository<ViewValidation, Long> {

    // 根据视图ID查询所有校验规则
    List<ViewValidation> findByViewId(Long viewId);

    // 删除视图的所有校验规则
    void deleteByViewId(Long viewId);
}

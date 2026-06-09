package com.vueadmin.repository;

import com.vueadmin.entity.quality.QualityCheckTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QualityCheckTaskRepository extends JpaRepository<QualityCheckTask, Long> {

    Optional<QualityCheckTask> findByTaskCode(String taskCode);

    List<QualityCheckTask> findByViewId(Long viewId);

    List<QualityCheckTask> findByStatus(String status);

    List<QualityCheckTask> findByCreatedBy(String createdBy);

    boolean existsByTaskCode(String taskCode);
}

package com.vueadmin.repository;

import com.vueadmin.entity.form.FormLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FormLogRepository extends JpaRepository<FormLog, Long> {

    /**
     * 根据表单ID查询日志列表（按时间倒序）
     */
    List<FormLog> findByFormIdOrderByCreatedAtDesc(Long formId);

    /**
     * 根据表单ID和操作类型查询日志
     */
    List<FormLog> findByFormIdAndOperationTypeOrderByCreatedAtDesc(Long formId, String operationType);
}

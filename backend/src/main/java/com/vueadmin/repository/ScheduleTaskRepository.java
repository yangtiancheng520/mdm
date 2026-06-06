package com.vueadmin.repository;

import com.vueadmin.entity.system.ScheduleTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 定时任务Repository
 */
@Repository
public interface ScheduleTaskRepository extends JpaRepository<ScheduleTask, Long> {

    Optional<ScheduleTask> findByTaskCode(String taskCode);

    List<ScheduleTask> findByStatus(String status);

    @Query("SELECT st FROM ScheduleTask st WHERE " +
           "(:taskCode IS NULL OR st.taskCode LIKE %:taskCode%) AND " +
           "(:taskName IS NULL OR st.taskName LIKE %:taskName%) AND " +
           "(:taskType IS NULL OR st.taskType = :taskType) AND " +
           "(:status IS NULL OR st.status = :status)")
    List<ScheduleTask> searchTasks(@Param("taskCode") String taskCode,
                                   @Param("taskName") String taskName,
                                   @Param("taskType") String taskType,
                                   @Param("status") String status);

    boolean existsByTaskCode(String taskCode);
}

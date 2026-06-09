package com.vueadmin.repository;

import com.vueadmin.entity.distribution.DisTopic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DisTopicRepository extends JpaRepository<DisTopic, Long> {

    Optional<DisTopic> findByTopicCode(String topicCode);

    boolean existsByTopicCode(String topicCode);

    List<DisTopic> findByStatus(String status);

    List<DisTopic> findByDataType(String dataType);

    @Query("SELECT t FROM DisTopic t WHERE " +
           "(:dataType IS NULL OR t.dataType = :dataType) AND " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:keyword IS NULL OR t.topicName LIKE %:keyword% OR t.topicCode LIKE %:keyword%)")
    Page<DisTopic> search(@Param("dataType") String dataType,
                          @Param("status") String status,
                          @Param("keyword") String keyword,
                          Pageable pageable);

    @Query("SELECT COUNT(t) FROM DisTopic t WHERE t.status = :status")
    long countByStatus(@Param("status") String status);
}

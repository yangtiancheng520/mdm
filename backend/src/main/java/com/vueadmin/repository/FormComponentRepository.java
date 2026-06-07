package com.vueadmin.repository;

import com.vueadmin.entity.form.FormComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FormComponentRepository extends JpaRepository<FormComponent, Long> {

    List<FormComponent> findByFormIdOrderByRowIndexAscColIndexAsc(Long formId);

    List<FormComponent> findByFormIdAndGroupIdOrderBySort(Long formId, Long groupId);

    List<FormComponent> findByGroupIdOrderBySort(Long groupId);

    void deleteByFormId(Long formId);

    void deleteByGroupId(Long groupId);

    @Query("SELECT MAX(c.rowIndex) FROM FormComponent c WHERE c.formId = :formId AND c.groupId = :groupId")
    Integer findMaxRowIndex(@Param("formId") Long formId, @Param("groupId") Long groupId);

    @Query("SELECT c FROM FormComponent c WHERE c.formId = :formId ORDER BY c.groupId, c.rowIndex, c.colIndex")
    List<FormComponent> findByFormIdOrdered(@Param("formId") Long formId);
}

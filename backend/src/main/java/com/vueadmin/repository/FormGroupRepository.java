package com.vueadmin.repository;

import com.vueadmin.entity.form.FormGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FormGroupRepository extends JpaRepository<FormGroup, Long> {

    List<FormGroup> findByFormIdOrderBySort(Long formId);

    void deleteByFormId(Long formId);

    boolean existsByFormIdAndGroupCode(Long formId, String groupCode);
}

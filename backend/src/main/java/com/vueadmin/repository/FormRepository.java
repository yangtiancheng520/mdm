package com.vueadmin.repository;

import com.vueadmin.entity.form.Form;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FormRepository extends JpaRepository<Form, Long> {

    Optional<Form> findByFormCode(String formCode);

    List<Form> findByViewId(Long viewId);

    List<Form> findByViewIdAndFormType(Long viewId, String formType);

    @Query("SELECT f FROM Form f WHERE " +
           "(:formName IS NULL OR f.formName LIKE %:formName%) AND " +
           "(:formType IS NULL OR f.formType = :formType) AND " +
           "(:viewId IS NULL OR f.viewId = :viewId) AND " +
           "(:status IS NULL OR f.status = :status)")
    List<Form> search(@Param("formName") String formName,
                      @Param("formType") String formType,
                      @Param("viewId") Long viewId,
                      @Param("status") String status);

    boolean existsByFormCode(String formCode);

    @Query("SELECT f FROM Form f WHERE f.viewId = :viewId AND f.formType = :formType AND f.isDefault = true")
    Optional<Form> findDefaultForm(@Param("viewId") Long viewId, @Param("formType") String formType);
}

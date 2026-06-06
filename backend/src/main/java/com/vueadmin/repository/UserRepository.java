package com.vueadmin.repository;

import com.vueadmin.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByAccount(String account);

    Optional<User> findByAccountAndPassword(String account, String password);

    @Query("SELECT u FROM User u WHERE " +
           "(:account IS NULL OR u.account LIKE CONCAT('%', :account, '%')) AND " +
           "(:name IS NULL OR u.name LIKE CONCAT('%', :name, '%')) AND " +
           "(:status IS NULL OR u.status = :status) AND " +
           "(:orgId IS NULL OR u.orgId = :orgId)")
    List<User> searchUsers(@Param("account") String account,
                           @Param("name") String name,
                           @Param("status") User.Status status,
                           @Param("orgId") Long orgId);
}

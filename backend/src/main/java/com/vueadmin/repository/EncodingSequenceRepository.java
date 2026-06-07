package com.vueadmin.repository;

import com.vueadmin.entity.standard.EncodingSequence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 编码序列号Repository
 */
@Repository
public interface EncodingSequenceRepository extends JpaRepository<EncodingSequence, Long> {

    /**
     * 根据规则ID和范围键查找（带悲观锁，用于序列号生成）
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<EncodingSequence> findByRuleIdAndScopeKey(Long ruleId, String scopeKey);

    /**
     * 重置序列号
     */
    @Modifying
    @Query("UPDATE EncodingSequence s SET s.currentValue = 0, s.resetDate = CURRENT_DATE WHERE s.id = :id")
    void resetSequence(Long id);
}

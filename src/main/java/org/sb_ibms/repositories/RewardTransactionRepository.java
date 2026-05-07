package org.sb_ibms.repositories;

import org.sb_ibms.models.RewardCard;
import org.sb_ibms.models.RewardTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RewardTransactionRepository extends JpaRepository<RewardTransaction, Long> {
    List<RewardTransaction> findByRewardCardOrderByTransactionDateDesc(RewardCard card);

    @Query("SELECT t FROM RewardTransaction t WHERE t.expiresAt IS NOT NULL " +
            "AND t.expiresAt < :now AND t.pointsEarned > 0")
    List<RewardTransaction> findExpirableTransactions(@Param("now") LocalDateTime now);
}

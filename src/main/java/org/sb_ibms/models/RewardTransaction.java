package org.sb_ibms.models;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class RewardTransaction {              // audit trail like CashbackDetails
    @Id
    @GeneratedValue
    private Long id;
    @ManyToOne
    private RewardCard rewardCard;
    private int pointsEarned;                 // or negative for redemption
    private String description;               // "Purchase at mall" or "Redeemed for cashback"
    private LocalDateTime timestamp;

    private LocalDateTime expiresAt;


    private Long cashbackId;
    @Column(nullable = false)
    private LocalDateTime transactionDate;// link to existing CashbackDetails if redeemed

    // Helper method
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

}
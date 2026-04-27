package org.sb_ibms.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.time.LocalDateTime;

@Entity
public class RewardTransaction {              // audit trail like your CashbackDetails
    @Id
    @GeneratedValue
    private Long id;
    @ManyToOne
    private RewardCard rewardCard;
    private int pointsEarned;                 // or negative for redemption
    private String description;               // "Purchase at mall" or "Redeemed for cashback"
    private LocalDateTime timestamp;
    private Long cashbackId;                  // link to your existing CashbackDetails if redeemed
}
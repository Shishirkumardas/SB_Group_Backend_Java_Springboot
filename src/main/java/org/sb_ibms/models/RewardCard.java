package org.sb_ibms.models;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class RewardCard {
    @Id
    @GeneratedValue
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User customer;                    // only SHOPPING_MALL_CUSTOMER

    private String cardNumber;                // auto-generate e.g. "SBMALL-XXXXXX"
    private int totalPoints = 0;
    private LocalDateTime issuedAt;
}

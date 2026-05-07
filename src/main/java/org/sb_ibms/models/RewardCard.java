package org.sb_ibms.models;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class RewardCard {
    @Id
    @GeneratedValue
    private Long id;

//    @OneToOne
//    @JoinColumn(name = "user_id", unique = true)
//    private User customer;                    // only SHOPPING_MALL_CUSTOMER

    @OneToOne
    @JoinColumn(name = "master_data_id", unique = true)
    private MasterData customer;

    private String cardNumber;                // auto-generate e.g. "SBMALL-XXXXXX"
    private int totalPoints = 0;
    private LocalDateTime issuedAt;

    @OneToMany(mappedBy = "rewardCard", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RewardTransaction> transactions = new ArrayList<>();
    private boolean isActive=true;

    private int expiringSoonPoints = 0;   // points expiring in next 30 days
    private LocalDateTime lastExpiryCheck;
}

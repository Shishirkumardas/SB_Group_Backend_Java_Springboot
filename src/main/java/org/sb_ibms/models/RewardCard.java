package org.sb_ibms.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "reward_card")
public class RewardCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

//    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "master_data_id", nullable = false, unique = true)
//    private MasterData customer;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shopping_mall_customer_id",nullable = true, unique = true)
    @JsonIgnore
    private ShoppingMallCustomer customer;

    @Column(name = "shopping_mall_id")
    private Long shoppingMallId;

    private String cardNumber;

    private int totalPoints = 0;

    private LocalDateTime issuedAt;

    @Column(name = "is_active")           // ← Explicit column name
    private boolean isActive = true;

    @OneToMany(mappedBy = "rewardCard", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RewardTransaction> transactions = new ArrayList<>();

    private int expiringSoonPoints = 0;

    private LocalDateTime lastExpiryCheck;
}
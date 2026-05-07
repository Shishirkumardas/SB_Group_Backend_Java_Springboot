package org.sb_ibms.models;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "shopping_mall_cashback")
@Data
public class ShoppingMallCashback {
    @Id
    private String id = UUID.randomUUID().toString();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private double amount;

    private String description;

    private String remarks;

    @Column(nullable = false)
    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime processedAt;

    private String transactionId;

    private String failureReason;


    public ShoppingMallCashback() {}

    public ShoppingMallCashback(User user, double amount, String description) {
        this.user = user;
        this.amount = amount;
        this.description = description;
        this.status = "PENDING";
        this.createdAt = LocalDateTime.now();
    }
}

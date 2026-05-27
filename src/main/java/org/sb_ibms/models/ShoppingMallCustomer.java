package org.sb_ibms.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.sb_ibms.enums.OrderStatus;
import org.sb_ibms.enums.PaymentMethod;
import org.sb_ibms.enums.ShoppingMallPaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "shopping_mall_customers")
public class ShoppingMallCustomer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shoppingmall_area_id", nullable = true)
    private ShoppingMallArea area;

    // Correct bidirectional mapping
    @OneToMany(mappedBy = "shoppingMallCustomer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShoppingMallPayments> payments = new ArrayList<>();

    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL)
    @JsonIgnore
    private RewardCard rewardCard;

    private BigDecimal quantity;

    @Enumerated(EnumType.STRING)
    private ShoppingMallPaymentMethod paymentMethod;
    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PENDING;

    private BigDecimal phone;
    private BigDecimal bkashNumber;
    private BigDecimal rocketNumber;
    private BigDecimal nogodNumber;
    private BigDecimal nid;
    private LocalDate date;
    private BigDecimal purchaseAmount;
    private BigDecimal paidAmount;
    private BigDecimal dueAmount;
    private BigDecimal cashBackAmount;

    private String remarks;
    private boolean paymentCompleted = false;

    private BigDecimal amountBackFromPurchase;
    private LocalDate nextDueDate;



    @PrePersist
    @PreUpdate
    public void calculateDue() {
        this.dueAmount = this.amountBackFromPurchase;
    }

    public boolean isPaymentCompleted() {
        if (paidAmount == null || purchaseAmount == null) return false;
        return paidAmount.compareTo(purchaseAmount) == 0;
    }
}
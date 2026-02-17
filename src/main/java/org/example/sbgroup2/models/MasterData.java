package org.example.sbgroup2.models;

import jakarta.persistence.*;
import lombok.Data;
import org.example.sbgroup2.enums.OrderStatus;
import org.example.sbgroup2.enums.PaymentMethod;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@Table(name = "master_data")
public class MasterData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id", nullable = false)
    private Area area;
    @OneToMany(
            mappedBy = "masterData",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Payment> payments;
    private BigDecimal quantity;
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;
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
    public boolean PaymentCompleted;
    private BigDecimal amountBackFromPurchase;
    private LocalDate nextDueDate;

    @PrePersist
    @PreUpdate
    public void calculateDue() {
        this.dueAmount=amountBackFromPurchase;
    }
    public boolean isPaymentCompleted() {
        if (paidAmount == null || purchaseAmount == null) {
            return false; // or handle nulls as per your logic
        }
        return paidAmount.equals(purchaseAmount);
    }


}

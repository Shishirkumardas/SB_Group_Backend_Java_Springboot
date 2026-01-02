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

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;
    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PENDING;

    private BigDecimal bkash;
    private LocalDate date;
    private BigDecimal purchaseAmount;
    private BigDecimal paidAmount;
    private BigDecimal dueAmount;
    private BigDecimal cashBackAmount;
    public boolean PaymentCompleted;

    @PrePersist
    @PreUpdate
    public void calculateDue() {
        if (paidAmount == null) {
            paidAmount = BigDecimal.ZERO;
        }
        if (purchaseAmount == null) {
            purchaseAmount = BigDecimal.ZERO;
        }
        this.dueAmount = purchaseAmount.subtract(paidAmount);
    }
    public boolean isPaymentCompleted() {
        if (paidAmount == null || purchaseAmount == null) {
            return false; // or handle nulls as per your logic
        }
        return paidAmount.equals(purchaseAmount);
    }


}

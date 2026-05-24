package org.sb_ibms.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sb_ibms.enums.PaymentMethod;
import org.sb_ibms.enums.PaymentStatus;
import org.sb_ibms.enums.ShoppingMallPaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingMallPayments {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "shopping_mall_customers_id", nullable = false)
    private ShoppingMallCustomer shoppingMallCustomer;
    @Enumerated(EnumType.STRING)
    private ShoppingMallPaymentMethod paymentMethod;
    private BigDecimal paidAmount;
    private LocalDate paymentDate;
    @Enumerated(EnumType.STRING)
    private PaymentStatus status = PaymentStatus.PENDING;
    private String trxId;
}

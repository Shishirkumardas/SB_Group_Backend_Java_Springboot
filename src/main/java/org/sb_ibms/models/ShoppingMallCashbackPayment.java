package org.sb_ibms.models;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@Table(name = "shoppingmall_cashback")
public class ShoppingMallCashbackPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private ShoppingMallCustomer masterData;

    private LocalDate paymentDate;

    private BigDecimal amount;
}

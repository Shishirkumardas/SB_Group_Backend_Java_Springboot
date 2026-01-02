package org.example.sbgroup2.models;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
public class CashbackPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private MasterData masterData;

    private LocalDate paymentDate;

    private BigDecimal amount;
}

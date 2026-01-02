package org.example.sbgroup2.models;

import jakarta.persistence.*;
import lombok.Data;
import org.example.sbgroup2.enums.OrderStatus;
import org.example.sbgroup2.enums.PaymentMethod;
import org.example.sbgroup2.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
public class Payment {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "master_data_id", nullable = false)
    private MasterData masterData;
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;
    private BigDecimal paidAmount;
    private LocalDate paymentDate;
    @Enumerated(EnumType.STRING)
    private PaymentStatus status = PaymentStatus.PENDING;


}


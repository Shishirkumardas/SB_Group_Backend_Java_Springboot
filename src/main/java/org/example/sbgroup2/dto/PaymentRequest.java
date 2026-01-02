package org.example.sbgroup2.dto;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.sbgroup2.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
public class PaymentRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private PaymentMethod paymentMethod;
    private BigDecimal amount;
    private LocalDate paymentDate;
}

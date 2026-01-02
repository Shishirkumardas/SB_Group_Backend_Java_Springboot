package org.example.sbgroup2.dto;

import lombok.Data;
import org.example.sbgroup2.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDate;
@Data
public class CustomerFormDTO {

    private String customerName;
    private BigDecimal amount;
    private LocalDate paymentDate;
    private PaymentMethod paymentMethod;

    // getters & setters
}


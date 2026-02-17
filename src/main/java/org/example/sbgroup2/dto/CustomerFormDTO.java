package org.example.sbgroup2.dto;

import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import org.example.sbgroup2.enums.PaymentMethod;
import org.example.sbgroup2.models.Area;

import java.math.BigDecimal;
import java.time.LocalDate;
@Data
public class CustomerFormDTO {

    private String customerName;
    private BigDecimal nid;

    private Long areaID;
    private BigDecimal phoneNumber;
    private LocalDate paymentDate;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private BigDecimal quantity;

}


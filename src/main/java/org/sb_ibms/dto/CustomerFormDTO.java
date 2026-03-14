package org.sb_ibms.dto;

import lombok.Data;
import org.sb_ibms.enums.PaymentMethod;

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


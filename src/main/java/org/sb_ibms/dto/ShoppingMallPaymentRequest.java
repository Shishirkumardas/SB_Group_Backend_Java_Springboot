package org.sb_ibms.dto;

import lombok.Data;
import org.sb_ibms.enums.PaymentMethod;
import org.sb_ibms.enums.ShoppingMallPaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDate;
@Data
public class ShoppingMallPaymentRequest {
    private ShoppingMallPaymentMethod paymentMethod;
    private BigDecimal amount;
    private LocalDate paymentDate;
    private BigDecimal paidAmount;
    private String trxId;
}

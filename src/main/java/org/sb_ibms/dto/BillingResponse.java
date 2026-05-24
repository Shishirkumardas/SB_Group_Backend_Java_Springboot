package org.sb_ibms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class BillingResponse {
    private String billNumber;
    private BigDecimal totalAmount;
    private boolean isMember;
    private String customerName;
    private String message;

    public BillingResponse(String billNumber, BigDecimal finalAmount, boolean isMember) {
    }
}

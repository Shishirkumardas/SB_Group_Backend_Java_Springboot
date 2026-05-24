package org.sb_ibms.dto;

import lombok.Data;
import org.sb_ibms.enums.ShoppingMallPaymentMethod;

import java.math.BigDecimal;
import java.util.List;

@Data
public class BillingRequest {
    private BigDecimal customerPhone;
    private List<CartItemRequest> items;
    private BigDecimal discountAmount;
    private ShoppingMallPaymentMethod paymentMethod;
    private String trxId;
}

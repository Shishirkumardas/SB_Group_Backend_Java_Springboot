package org.sb_ibms.dto;

import lombok.Data;
import org.sb_ibms.enums.ShoppingMallPaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ShoppingMallPaymentView {

    private LocalDate date;
    private BigDecimal paidAmount;
    private ShoppingMallPaymentMethod paymentMethod;
    private String customerName;
    private String customerPhone;
//    private Long shoppingMallId;

    // Constructor matching the query
    public ShoppingMallPaymentView(LocalDate date,
                                   BigDecimal paidAmount,
                                   ShoppingMallPaymentMethod paymentMethod,
                                   String customerName,
                                   BigDecimal customerPhone) {
        this.date = date;
        this.paidAmount = paidAmount;
        this.paymentMethod = paymentMethod;
        this.customerName = customerName;
        this.customerPhone = "0"+ customerPhone.toBigInteger();
//        this.shoppingMallId = ShoppingMallId;
    }
}
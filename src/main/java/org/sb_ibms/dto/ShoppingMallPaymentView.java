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

    // Optional: Useful for debugging or future frontend filtering
    private Long shoppingMallId;

    // Main Constructor (matching your current query)
    public ShoppingMallPaymentView(
            LocalDate date,
            BigDecimal paidAmount,
            ShoppingMallPaymentMethod paymentMethod,
            String customerName,
            BigDecimal customerPhone) {

        this.date = date;
        this.paidAmount = paidAmount;
        this.paymentMethod = paymentMethod;
        this.customerName = customerName;

        // Safe phone formatting with leading zero
        this.customerPhone = (customerPhone != null)
                ? "0" + customerPhone.toBigInteger()
                : "N/A";
    }

    // Overloaded Constructor (if you want to pass mallId later)
    public ShoppingMallPaymentView(
            LocalDate date,
            BigDecimal paidAmount,
            ShoppingMallPaymentMethod paymentMethod,
            String customerName,
            BigDecimal customerPhone,
            Long shoppingMallId) {

        this(date, paidAmount, paymentMethod, customerName, customerPhone);
        this.shoppingMallId = shoppingMallId;
    }
}
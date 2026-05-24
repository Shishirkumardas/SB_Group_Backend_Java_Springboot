package org.sb_ibms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CartItemDetail {

    private String productName;
    private Integer quantity;
    private BigDecimal totalAmount;

}

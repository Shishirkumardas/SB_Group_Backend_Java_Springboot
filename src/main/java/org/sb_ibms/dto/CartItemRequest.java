package org.sb_ibms.dto;

import lombok.Data;

@Data
public class CartItemRequest {
    private String barcode;
    private Integer quantity;
}

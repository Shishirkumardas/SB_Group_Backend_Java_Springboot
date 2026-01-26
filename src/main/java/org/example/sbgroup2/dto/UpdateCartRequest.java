package org.example.sbgroup2.dto;

import lombok.Data;

@Data
public class UpdateCartRequest {
    private Long itemId;
    private int quantity;
}

package org.sb_ibms.dto;

import lombok.Data;

@Data
public class UpdateCartRequest {
    private Long itemId;
    private int quantity;
}

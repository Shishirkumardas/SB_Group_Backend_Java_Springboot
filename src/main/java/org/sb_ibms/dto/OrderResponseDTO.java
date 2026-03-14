package org.sb_ibms.dto;

import java.time.LocalDateTime;
import java.util.List;

public class OrderResponseDTO {
    public Long id;
    public LocalDateTime orderDate;
    public String status;
    public double totalAmount;
    public List<OrderItemResponseDTO> items;
    public String address;
}

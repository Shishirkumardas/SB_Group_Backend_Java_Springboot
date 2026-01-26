package org.example.sbgroup2.controller;

import lombok.RequiredArgsConstructor;

import org.example.sbgroup2.dto.OrderResponseDTO;
import org.example.sbgroup2.enums.OrderStatus;
import org.example.sbgroup2.models.Order;
import org.example.sbgroup2.services.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<List<OrderResponseDTO>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrdersForAdmin());
    }

    // ✅ UPDATE STATUS
    @PutMapping("{id}/status")
    public ResponseEntity<OrderResponseDTO> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status
    ) {
        Order updatedOrder = orderService.updateStatus(id, status);
        return ResponseEntity.ok(toDTO(updatedOrder));
    }

    // 🔁 ENTITY → DTO MAPPER
    private OrderResponseDTO toDTO(Order order) {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.id = order.getId();
        dto.orderDate = order.getOrderDate();
        dto.status = order.getStatus().name();
        dto.totalAmount = order.getTotalAmount();
        return dto;
    }
}
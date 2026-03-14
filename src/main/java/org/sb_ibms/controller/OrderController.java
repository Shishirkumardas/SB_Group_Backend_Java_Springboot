package org.sb_ibms.controller;



import org.sb_ibms.dto.OrderResponseDTO;
import org.sb_ibms.enums.OrderStatus;
import org.sb_ibms.models.CartItem;
import org.sb_ibms.models.Order;
import org.sb_ibms.models.OrderItem;
import org.sb_ibms.models.User;
import org.sb_ibms.repositories.CartItemRepository;
import org.sb_ibms.repositories.OrderItemRepository;
import org.sb_ibms.repositories.OrderRepository;
import org.sb_ibms.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/create")
    public Order createOrder(@RequestParam String userId, @RequestBody String shippingAddress) {
        User user = userRepository.findById(userId).orElseThrow();
        user.setAddress(shippingAddress); // Update address
        userRepository.save(user);

        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        double total = cartItems.stream().mapToDouble(item -> item.getQuantity() * item.getProduct().getPrice()).sum();

        Order order = Order.builder()
                .user(user)
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.PENDING)
                .totalAmount(total)
                .build();
        order = orderRepository.save(order);


        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = OrderItem.builder()
                    .product(cartItem.getProduct())
                    .quantity(cartItem.getQuantity())
                    .price(cartItem.getProduct().getPrice())
                    .build();

            order.addOrderItem(orderItem);   // sets both sides
        }
        order = orderRepository.save(order);

        // Clear cart
        cartItemRepository.deleteAll(cartItems);

        return order;
    }


    @GetMapping
    public List<OrderResponseDTO> getUserOrders(@RequestParam String userId) {
        return orderRepository.findByUserId(userId)
                .stream()
                .map(order -> {
                    OrderResponseDTO dto = new OrderResponseDTO();
                    dto.id = order.getId();
                    dto.orderDate = order.getOrderDate();
                    dto.status = order.getStatus().name();
                    dto.totalAmount = order.getTotalAmount();
                    return dto;
                })
                .toList();
    }


}

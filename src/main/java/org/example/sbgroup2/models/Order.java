package org.example.sbgroup2.models;

import jakarta.persistence.*;
import lombok.*;
import org.example.sbgroup2.enums.OrderStatus;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private double totalAmount;

    @ManyToOne
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    // Helper method (very useful)
//    public void addOrderItem(OrderItem item) {
//        orderItems.add(item);
//        item.setOrder(this);
//    }
//
//    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<OrderItem> orderItems = new ArrayList<>();   // ← Initialize here!

//    // Getter & Setter
//    public List<OrderItem> getOrderItems() {
//        return orderItems;
//    }
//
//    public void setOrderItems(List<OrderItem> orderItems) {
//        this.orderItems = orderItems;
//    }

//    // Helper method (this is probably line 39)
    public void addOrderItem(OrderItem item) {
        // this.orderItems.add(item);   ← this line crashes if orderItems == null
        orderItems.add(item);           // now safe
        item.setOrder(this);            // important for bidirectional relationship
    }
//
//    // Optional: remove method
//    public void removeOrderItem(OrderItem item) {
//        orderItems.remove(item);
//        item.setOrder(null);
//    }
}

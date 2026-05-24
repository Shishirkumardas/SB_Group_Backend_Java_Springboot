package org.sb_ibms.models;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "sales")
public class ShoppingMallSale {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "shopping_mall_customer_id")
    private ShoppingMallCustomer customer;

    private LocalDate saleDate;
    private BigDecimal totalAmount;
    private BigDecimal discount;
    private BigDecimal netAmount;



    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL)
    private List<ShoppingMallSaleItem> items = new ArrayList<>();

    // Points & Cashback
    private int pointsEarned;
    private BigDecimal cashbackGiven;
}

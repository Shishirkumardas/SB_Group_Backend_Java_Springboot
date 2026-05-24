package org.sb_ibms.models;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "shoppingmall_products")
@Data
public class ShoppingMallProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String barcode;
    private String name;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private Integer stock;
    private String category;
}

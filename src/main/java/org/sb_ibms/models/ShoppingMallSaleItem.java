package org.sb_ibms.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Data
public class ShoppingMallSaleItem {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private ShoppingMallSale sale;

    @ManyToOne
    private Product product;

    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
}

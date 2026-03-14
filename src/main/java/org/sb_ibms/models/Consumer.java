package org.sb_ibms.models;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Data
@Table(name = "consumers")
public class Consumer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;


    private String area;

    private BigDecimal purchaseAmount;
    private BigDecimal paidAmount;
    private BigDecimal dueAmount;
}


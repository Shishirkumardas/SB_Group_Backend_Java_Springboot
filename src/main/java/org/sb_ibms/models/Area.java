package org.sb_ibms.models;


import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;


@Entity
@Data

public class Area {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private BigDecimal CashbackAmount;
    private BigDecimal PackageQuantity;

    private BigDecimal purchaseAmount;
    private BigDecimal paidAmount;
    private BigDecimal dueAmount;



}


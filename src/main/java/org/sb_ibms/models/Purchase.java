package org.sb_ibms.models;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
public class Purchase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Consumer consumer;
    private BigDecimal amount;
    private int purchaseAmount;
    private LocalDate purchaseDate;
}


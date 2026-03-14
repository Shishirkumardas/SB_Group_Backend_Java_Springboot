package org.sb_ibms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class ConsumerView {
    private String name;
    private String area;
    private BigDecimal totalPurchase;
    private BigDecimal totalPaid;
    private BigDecimal totalDue;
}


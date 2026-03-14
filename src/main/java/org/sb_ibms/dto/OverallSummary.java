package org.sb_ibms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class OverallSummary {
    private BigDecimal totalPurchase;
    private BigDecimal totalPaid;
    private BigDecimal totalDue;
}


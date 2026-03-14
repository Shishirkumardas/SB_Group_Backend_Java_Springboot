package org.sb_ibms.services;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class AreaSummaryDTO {
    private Long areaId;
    private String areaName;
    private BigDecimal totalPurchase;
    private BigDecimal totalQuantity;
    private BigDecimal totalCashback;
    private Long cashbackQuantity;
}

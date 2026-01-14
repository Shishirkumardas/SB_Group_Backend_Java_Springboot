package org.example.sbgroup2.services;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

public record DashboardSummaryDTO(
        BigDecimal totalPurchase,
        BigDecimal totalPaid,
        BigDecimal totalDue,
        double paidPercent,
        BigDecimal totalCashbackPaid,
        long totalConsumers,
        BigDecimal averagePurchase
) {}


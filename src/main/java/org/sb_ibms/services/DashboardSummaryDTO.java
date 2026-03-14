package org.sb_ibms.services;
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


package org.sb_ibms.services;

import lombok.AllArgsConstructor;
import org.sb_ibms.models.MasterData;
import org.sb_ibms.repositories.MasterDataRepository;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;


@Service
@AllArgsConstructor
public class DashboardService {

    private final MasterDataRepository masterDataRepo;

    public DashboardSummaryDTO getSummary() {
        // Total Purchase
        BigDecimal totalPurchase = masterDataRepo.findAll()
                .stream()
                .map(MasterData::getPurchaseAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Total Due (from MasterData)
        BigDecimal totalDue = masterDataRepo.findAll()
                .stream()
                .map(MasterData::getAmountBackFromPurchase)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Total Cashback Paid = Total Purchase - Total Due
        BigDecimal totalCashbackPaid = totalPurchase.subtract(totalDue);

        // Paid Percentage
        double paidPercent = totalPurchase.compareTo(BigDecimal.ZERO) > 0
                ? totalCashbackPaid.multiply(BigDecimal.valueOf(100))
                .divide(totalPurchase, 2, RoundingMode.HALF_UP).doubleValue()
                : 0.0;

        // Extra useful metrics
        long totalConsumers = masterDataRepo.count();
        BigDecimal avgPurchase = totalConsumers > 0
                ? totalPurchase.divide(BigDecimal.valueOf(totalConsumers), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return new DashboardSummaryDTO(
                totalPurchase,
                totalCashbackPaid,
                totalDue,
                paidPercent,
                totalCashbackPaid,
                totalConsumers,
                avgPurchase
        );
    }
}

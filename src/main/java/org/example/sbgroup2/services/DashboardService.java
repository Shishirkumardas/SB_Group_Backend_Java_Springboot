package org.example.sbgroup2.services;

import lombok.RequiredArgsConstructor;
import org.example.sbgroup2.models.MasterData;
import org.example.sbgroup2.models.Payment;
import org.example.sbgroup2.models.Purchase;
import org.example.sbgroup2.repositories.ConsumerRepository;
import org.example.sbgroup2.repositories.MasterDataRepository;
import org.example.sbgroup2.repositories.PaymentRepository;
import org.example.sbgroup2.repositories.PurchaseRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;


@Service
@RequiredArgsConstructor
public class DashboardService {

    private final PurchaseRepository purchaseRepo;
    private final PaymentRepository paymentRepo;
    private final ConsumerRepository consumerRepo;
    private final MasterDataRepository masterDataRepo;

    public DashboardSummaryDTO getSummary() {
        // Total Purchase
        BigDecimal totalPurchase = masterDataRepo.findAll()
                .stream()
                .map(MasterData::getPurchaseAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Total Paid


        // Total Due (from MasterData)
        BigDecimal totalDue = masterDataRepo.findAll()
                .stream()
                .map(MasterData::getAmountBackFromPurchase)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Total Cashback Paid = Total Purchase - Total Due
        BigDecimal totalCashbackPaid = totalPurchase.subtract(totalDue);
        BigDecimal totalPaid=totalCashbackPaid;

        // Paid Percentage
        double paidPercent = totalPurchase.compareTo(BigDecimal.ZERO) > 0
                ? totalPaid.multiply(BigDecimal.valueOf(100))
                .divide(totalPurchase, 2, RoundingMode.HALF_UP).doubleValue()
                : 0.0;

        // Extra useful metrics
        long totalConsumers = masterDataRepo.count();
        BigDecimal avgPurchase = totalConsumers > 0
                ? totalPurchase.divide(BigDecimal.valueOf(totalConsumers), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return new DashboardSummaryDTO(
                totalPurchase,
                totalPaid,
                totalDue,
                paidPercent,
                totalCashbackPaid,
                totalConsumers,
                avgPurchase
        );
    }
}

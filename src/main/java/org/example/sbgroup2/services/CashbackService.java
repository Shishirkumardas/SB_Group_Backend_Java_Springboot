package org.example.sbgroup2.services;

import lombok.RequiredArgsConstructor;
import org.example.sbgroup2.dto.CashbackDetailsDTO;
import org.example.sbgroup2.models.CashbackPayment;
import org.example.sbgroup2.models.MasterData;
import org.example.sbgroup2.repositories.CashbackPaymentRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class CashbackService {

    private final CashbackPaymentRepository cashbackRepo;

    public CashbackDetailsDTO calculateCashback(MasterData master) {

        BigDecimal purchaseAmount = master.getPurchaseAmount();
        LocalDate purchaseDate = master.getDate();

        if (purchaseAmount == null || purchaseAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        BigDecimal monthlyCashback = purchaseAmount.multiply(BigDecimal.valueOf(0.10));
        LocalDate cashbackStartDate = purchaseDate.plusDays(30);

        BigDecimal totalPaid = cashbackRepo
                .findByMasterDataId(master.getId())
                .stream()
                .map(CashbackPayment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalMonths = purchaseAmount.divide(monthlyCashback).longValue();
        long paidMonths = totalPaid.divide(monthlyCashback, 0, RoundingMode.DOWN).longValue();

        LocalDate today = LocalDate.now();

        long monthsPassed = cashbackStartDate.isAfter(today)
                ? 0
                : ChronoUnit.MONTHS.between(cashbackStartDate, today) + 1;

        long missedMonths = Math.max(0, monthsPassed - paidMonths);
        BigDecimal missedAmount = monthlyCashback.multiply(BigDecimal.valueOf(missedMonths));

        boolean completed = paidMonths >= totalMonths;

        LocalDate nextDueDate = completed
                ? null
                : cashbackStartDate.plusMonths(paidMonths);

        return new CashbackDetailsDTO(
                cashbackStartDate,
                cashbackStartDate,
                cashbackStartDate.plusMonths(totalMonths - 1),
                monthlyCashback,
                missedAmount,
                missedMonths,
                missedMonths > 0 ? cashbackStartDate.plusMonths(paidMonths) : null,
                nextDueDate,
                completed ? BigDecimal.ZERO : monthlyCashback,
                nextDueDate,
                cashbackStartDate,
                completed ? "COMPLETED" : missedMonths > 0 ? "OVERDUE" : "ACTIVE"
        );
    }
}

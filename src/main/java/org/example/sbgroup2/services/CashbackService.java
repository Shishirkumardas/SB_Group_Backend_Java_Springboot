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
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

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
                purchaseDate,
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

    //For Excel Reading
    public CashbackDetailsDTO calculateCashback2(MasterData master) {

        if (master.getPurchaseAmount() == null ||
                master.getPurchaseAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return createEmptyDetails();
        }

        BigDecimal totalPurchase = master.getPurchaseAmount();
        LocalDate purchaseDate = master.getDate();
        LocalDate today = LocalDate.now(); // 2026-01-10 in your case

        // 1. Get ALL actual recorded payments
        List<CashbackPayment> payments = cashbackRepo.findByMasterDataId(master.getId());

        // Sort by payment month for easier calculation
        payments.sort(Comparator.comparing(CashbackPayment::getPaymentDate));

        // 2. Calculate key metrics from real data
        BigDecimal totalPaidCashback = payments.stream()
                .map(CashbackPayment::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal remainingCashback = totalPurchase.subtract(totalPaidCashback);

        // Expected monthly cashback amount (still 10% of purchase / total months)
        BigDecimal expectedMonthly = totalPurchase.multiply(BigDecimal.valueOf(0.10));

        // 3. Find how many months should have been paid by today
        // (This assumes payments expected every month after grace period)
        LocalDate firstExpectedPayment = purchaseDate.plusMonths(1); // or plusDays(30)
        long expectedPaymentsCount = ChronoUnit.MONTHS.between(firstExpectedPayment, today) + 1;
        if (expectedPaymentsCount < 0) expectedPaymentsCount = 0;

        long actualPaidCount = payments.size(); // number of payment records

        // 4. Find last paid month and next due
        LocalDate lastPaidDate = payments.isEmpty()
                ? null
                : payments.get(payments.size() - 1).getPaymentDate();

        LocalDate nextDueDate;
        if (remainingCashback.compareTo(BigDecimal.ZERO) <= 0) {
            nextDueDate = null;
        } else if (lastPaidDate == null) {
            nextDueDate = firstExpectedPayment;
        } else {
            // Next month after last payment
            nextDueDate = lastPaidDate.plusMonths(1);
        }

        // 5. Missed count & amount (only count months that are past due)
        long missedCount = Math.max(0, expectedPaymentsCount - actualPaidCount);
        BigDecimal missedAmount = expectedMonthly.multiply(BigDecimal.valueOf(missedCount));

        // 6. Status determination
        String status;
        if (remainingCashback.compareTo(BigDecimal.ZERO) <= 0) {
            status = "COMPLETED";
        } else if (missedCount > 0) {
            status = "OVERDUE";
        } else {
            status = "ACTIVE";
        }

        // 7. Build DTO (adjust fields according to your actual DTO structure)
        return new CashbackDetailsDTO(
                purchaseDate,
                firstExpectedPayment,                    // startDate
                firstExpectedPayment,                    // firstDueDate
                firstExpectedPayment.plusMonths(10),     // estimated end date (10 months typical)
                expectedMonthly,                         // monthly amount
                missedAmount,                            // total missed
                missedCount,                             // count of missed months
                missedCount > 0 ? nextDueDate : null,    // overdue since
                nextDueDate,                             // next due
                remainingCashback,                       // remaining to pay
                nextDueDate,
                firstExpectedPayment,
                status
        );
    }

    private CashbackDetailsDTO createEmptyDetails() {
        return new CashbackDetailsDTO(
                null, null, null, null, BigDecimal.ZERO,
                BigDecimal.ZERO, 0, null, null,
                BigDecimal.ZERO, null, null, "NOT_STARTED"
        );
    }
}

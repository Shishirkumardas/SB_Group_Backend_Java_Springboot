package org.sb_ibms.services;

import lombok.RequiredArgsConstructor;
import org.sb_ibms.dto.CashbackDetailsDTO;
import org.sb_ibms.models.*;
import org.sb_ibms.repositories.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShoppingMallCashbackService {
    private final ShoppingMallCahbackPaymentRepository shoppingMallCashbackRepo;
    private final ShoppingMallCustomerRepository masterDataRepository;



    public List<CashbackDetailsDTO> getCashbacksByNextDueDate(LocalDate inputDate) {
        LocalDate today = (inputDate != null) ? inputDate : LocalDate.now();

        return masterDataRepository.findAll().stream()
                .map(this::calculateCashback2)
                .filter(dto -> hasDueDateOnOrBefore(dto, today))
                .collect(Collectors.toList());
    }

    private boolean hasDueDateOnOrBefore(CashbackDetailsDTO dto, LocalDate today) {
        // Primary check: Next Due Date
        if (dto.getNextDueDate() != null) {
            return dto.getNextDueDate().isBefore(today) || dto.getNextDueDate().isEqual(today);
        }

        // Fallback: Check any upcoming due date
        if (dto.getUpcomingDueDates() != null) {
            return dto.getUpcomingDueDates().stream()
                    .anyMatch(due -> due != null &&
                            (due.isBefore(today) || due.isEqual(today)));
        }

        return false;
    }


    public List<CashbackDetailsDTO> getCashbackByPurchaseDate(LocalDate inputDate) {
        if (inputDate == null) {
            return List.of();
        }

        int targetDay = inputDate.getDayOfMonth();

        return masterDataRepository.findAll().stream()
                .map(this::calculateCashback2)           // Keep your calculation
                .filter(dto -> {
                    if (dto.getPurchaseDate() == null) {
                        return false;
                    }

                    // Main condition: Purchase was made on the same day of any month
                    return dto.getPurchaseDate().getDayOfMonth() == targetDay;
                })
                .collect(Collectors.toList());
    }


    //For Excel Reading
    public CashbackDetailsDTO calculateCashback2(ShoppingMallCustomer master) {

        if (master.getQuantity() == null ||
                master.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            return createEmptyDetails();
        }
        if (master.getPurchaseAmount() == null ||
                master.getPurchaseAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return createEmptyDetails();
        }

        BigDecimal quantity = master.getQuantity();
        ShoppingMallArea area = master.getArea();
        String areaName = area != null ? area.getName() : "Unknown Area";
        BigDecimal totalPurchase = master.getPurchaseAmount();
        String name =master.getName();
        LocalDate purchaseDate = master.getDate();
        LocalDate today = LocalDate.now();
        String phoneNumber = "0" + Objects.toString(master.getPhone() != null
                ? master.getPhone().longValue()
                : "", "");
        String paymentMethod = Objects.toString(master.getPaymentMethod(), "");

        List<ShoppingMallCashbackPayment> payments = shoppingMallCashbackRepo.findByMasterDataId(master.getId());

        payments.sort(Comparator.comparing(ShoppingMallCashbackPayment::getPaymentDate));

        BigDecimal totalPaidCashback = payments.stream()
                .map(ShoppingMallCashbackPayment::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal remainingCashback = totalPurchase.subtract(totalPaidCashback);

        BigDecimal expectedMonthly = totalPurchase.multiply(BigDecimal.valueOf(0.10));

        LocalDate firstExpectedPayment = purchaseDate.plusMonths(1); // or plusDays(30)
        long expectedPaymentsCount = ChronoUnit.MONTHS.between(firstExpectedPayment, today) + 1;
        if (expectedPaymentsCount < 0) expectedPaymentsCount = 0;

        long actualPaidCount = payments.size();
        LocalDate lastPaidDate = payments.isEmpty()
                ? null
                : payments.get(payments.size() - 1).getPaymentDate();

        LocalDate nextDueDate;
        if (remainingCashback.compareTo(BigDecimal.ZERO) <= 0) {
            nextDueDate = null;
        } else if (lastPaidDate == null) {
            nextDueDate = firstExpectedPayment;
        } else {
            nextDueDate = lastPaidDate.plusMonths(1);
        }

        long totalExpectedMonths = totalPurchase
                .divide(expectedMonthly, 0, RoundingMode.CEILING)
                .longValue();
        List<LocalDate> allDueDates = new ArrayList<>();
        LocalDate currentDue = firstExpectedPayment;

        for (long i = 0; i < totalExpectedMonths; i++) {
            allDueDates.add(currentDue);
            currentDue = currentDue.plusMonths(1);
        }

        List<LocalDate> upcoming = allDueDates.stream()
                .filter(d -> !d.isBefore(today))
                .sorted()
                .toList();


        master.setNextDueDate(nextDueDate);
        masterDataRepository.save(master);

        long missedCount = Math.max(0, expectedPaymentsCount - actualPaidCount);
        BigDecimal missedAmount = expectedMonthly.multiply(BigDecimal.valueOf(missedCount));


        String status;
        if (remainingCashback.compareTo(BigDecimal.ZERO) <= 0) {
            status = "COMPLETED";
        } else if (missedCount > 0) {
            status = "OVERDUE";
        } else {
            status = "ACTIVE";
        }

        return new CashbackDetailsDTO(
                name,
                quantity,
                purchaseDate,
                totalPurchase,
                phoneNumber,
                paymentMethod,
                firstExpectedPayment,
                firstExpectedPayment,
                firstExpectedPayment.plusMonths(10),
                expectedMonthly,
                missedAmount,
                missedCount,
                upcoming,
                missedCount > 0 ? nextDueDate : null,
                nextDueDate,
                remainingCashback,
                nextDueDate,
                firstExpectedPayment,
                status,
                areaName

        );
    }

    private CashbackDetailsDTO createEmptyDetails() {
        return new CashbackDetailsDTO(
                "NOT_STARTED",null,null, BigDecimal.ZERO,"NO Number","No method mentioned",null, null, null, BigDecimal.ZERO,
                BigDecimal.ZERO, 0, new ArrayList<>(),null, null,
                BigDecimal.ZERO, null, null, "NOT_STARTED",""
        );
    }

}

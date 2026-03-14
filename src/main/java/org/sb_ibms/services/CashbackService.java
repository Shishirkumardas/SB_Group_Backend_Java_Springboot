package org.sb_ibms.services;

import lombok.RequiredArgsConstructor;
import org.sb_ibms.dto.CashbackDetailsDTO;
import org.sb_ibms.models.CashbackPayment;
import org.sb_ibms.models.MasterData;
import org.sb_ibms.repositories.CashbackPaymentRepository;
import org.sb_ibms.repositories.MasterDataRepository;
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
public class CashbackService {

    private final CashbackPaymentRepository cashbackRepo;
    private final MasterDataRepository masterDataRepository;

    public List<CashbackDetailsDTO> getCashbacksByNextDueDate(LocalDate date) {

        List<MasterData> allMasters = masterDataRepository.findAll();

        List<CashbackDetailsDTO> result = new ArrayList<>();

        for (MasterData master : allMasters) {
            CashbackDetailsDTO dto = calculateCashback2(master);

            if(dto.getUpcomingDueDates() != null && dto.getUpcomingDueDates().contains(date)) {
                result.add(dto);
            }
            else if (dto.getNextDueDate() != null &&
                    dto.getNextDueDate().equals(date)) {
                result.add(dto);
            }
        }

        return result;
    }
    //Cashback List according
    public List<CashbackDetailsDTO> getCashbackByPurchaseDate(LocalDate inputDate) {
        int targetDay = inputDate.getDayOfMonth();

        return masterDataRepository.findAll().stream()
                .map(this::calculateCashback2)
                .filter(dto -> {
                    // Check next due date
                    if (dto.getNextDueDate() != null &&
                            dto.getNextDueDate().getDayOfMonth() == targetDay && dto.getPurchaseDate().getDayOfMonth()==targetDay
                            && dto.getPurchaseDate()!=inputDate) {
                        return true;
                    }

                    // Check any of the upcoming due dates
                    if (dto.getUpcomingDueDates() != null) {
                        return dto.getUpcomingDueDates().stream()
                                .anyMatch(due -> due != null && due.getDayOfMonth() == targetDay);
                    }

                    return false;
                })
                .collect(Collectors.toList());
    }


    //For Excel Reading
    public CashbackDetailsDTO calculateCashback2(MasterData master) {

        if (master.getQuantity() == null ||
                master.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            return createEmptyDetails();
        }
        if (master.getPurchaseAmount() == null ||
                master.getPurchaseAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return createEmptyDetails();
        }

        BigDecimal quantity = master.getQuantity();

        BigDecimal totalPurchase = master.getPurchaseAmount();
        String name =master.getName();
        LocalDate purchaseDate = master.getDate();
        LocalDate today = LocalDate.now();
        String phoneNumber = "0" + Objects.toString(master.getPhone() != null
                ? master.getPhone().longValue()
                : "", "");
        String paymentMethod = Objects.toString(master.getPaymentMethod(), "");

        List<CashbackPayment> payments = cashbackRepo.findByMasterDataId(master.getId());

        payments.sort(Comparator.comparing(CashbackPayment::getPaymentDate));

        BigDecimal totalPaidCashback = payments.stream()
                .map(CashbackPayment::getAmount)
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
                status
        );
    }

    private CashbackDetailsDTO createEmptyDetails() {
        return new CashbackDetailsDTO(
                "NOT_STARTED",null,null, BigDecimal.ZERO,"NO Number","No method mentioned",null, null, null, BigDecimal.ZERO,
                BigDecimal.ZERO, 0, new ArrayList<>(),null, null,
                BigDecimal.ZERO, null, null, "NOT_STARTED"
        );
    }
}

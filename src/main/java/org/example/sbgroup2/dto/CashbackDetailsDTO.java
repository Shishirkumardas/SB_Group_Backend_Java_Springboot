package org.example.sbgroup2.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.sbgroup2.repositories.MasterDataRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class CashbackDetailsDTO {

    private String name;
    private LocalDate purchaseDate;
    private BigDecimal totalPurchase;

    private String phoneNumber;
    private String paymentMethod;

    private LocalDate cashbackStartDate;
    private LocalDate firstDueMonth;
    private LocalDate lastDueMonth;
//
    private BigDecimal expectedMonthlyCashbackAmount;

    private BigDecimal missedCashbackAmount;
    private long missedCashbackCount;
    private List<LocalDate> upcomingDueDates = new ArrayList<>();
    private LocalDate earliestMissedDueDate;

    private LocalDate nextDueDate;
    private BigDecimal nextDueAmount;
    private LocalDate nextDueMonth;

    private LocalDate earliestDueMonth;

    private String cashbackStatus;
}

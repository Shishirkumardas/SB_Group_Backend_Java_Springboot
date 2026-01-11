package org.example.sbgroup2.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class CashbackDetailsDTO {
    private LocalDate purchaseDate;

    private LocalDate cashbackStartDate;
    private LocalDate firstDueMonth;
    private LocalDate lastDueMonth;
//
    private BigDecimal expectedMonthlyCashbackAmount;

    private BigDecimal missedCashbackAmount;
    private long missedCashbackCount;

    private LocalDate earliestMissedDueDate;

    private LocalDate nextDueDate;
    private BigDecimal nextDueAmount;
    private LocalDate nextDueMonth;

    private LocalDate earliestDueMonth;

    private String cashbackStatus;
}

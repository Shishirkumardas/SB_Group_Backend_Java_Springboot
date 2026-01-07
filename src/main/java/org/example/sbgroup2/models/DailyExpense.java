package org.example.sbgroup2.models;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

import static java.lang.Long.sum;

@Entity
@Data
@Table(name = "daily_expense")
public class DailyExpense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate date;
    private String name;
    private String expenseHead;
    private BigDecimal expenseAmount;
    private BigDecimal cashIn;
    private BigDecimal cashOut;
    private BigDecimal runningBalance;
    private String remarks;
    private BigDecimal openingBalance= BigDecimal.valueOf(0);
    private BigDecimal closingBalance;
}

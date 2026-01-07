package org.example.sbgroup2.repositories;

import org.example.sbgroup2.models.DailyExpense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyExpenseRepository extends JpaRepository<DailyExpense, Long> {

    List<DailyExpense> findByDate(LocalDate date);

    @Query("""
        SELECT COALESCE(MAX(d.closingBalance), 0)
        FROM DailyExpense d
        WHERE d.date < :date
    """)
    BigDecimal findPreviousClosingBalance(@Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(d.cashIn), 0) FROM DailyExpense d")
    BigDecimal totalDeposit();

    @Query("SELECT COALESCE(SUM(d.expenseAmount), 0) FROM DailyExpense d")
    BigDecimal totalExpense();

    @Query("SELECT COALESCE(SUM(d.cashOut), 0) FROM DailyExpense d")
    BigDecimal totalPaid();

    // Previous row before given date+id
    Optional<DailyExpense> findTopByDateLessThanEqualAndIdLessThanOrderByDateDescIdDesc(
            LocalDate date, Long id
    );

    // Last row overall (for create)
    Optional<DailyExpense> findTopByOrderByDateDescIdDesc();

    List<DailyExpense> findByDateGreaterThanEqualOrderByDateAscIdAsc(LocalDate date);

    // First row opening balance (ordered by id asc)
    @Query("SELECT d.openingBalance FROM DailyExpense d ORDER BY d.id ASC")
    Optional<BigDecimal> findFirstOpeningBalance();

    // Last row running balance (ordered by id desc)
    @Query("SELECT d.runningBalance FROM DailyExpense d ORDER BY d.id DESC")
    Optional<BigDecimal> findLastRunningBalance();
}

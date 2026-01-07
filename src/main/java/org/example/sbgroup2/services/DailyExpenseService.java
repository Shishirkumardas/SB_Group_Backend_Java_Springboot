package org.example.sbgroup2.services;

import jakarta.transaction.Transactional;
import org.example.sbgroup2.models.DailyExpense;
import org.example.sbgroup2.models.MasterData;
import org.example.sbgroup2.repositories.DailyExpenseRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class DailyExpenseService {

    private final DailyExpenseRepository repository;

    public DailyExpenseService(DailyExpenseRepository repository) {
        this.repository = repository;
    }

    public DailyExpense save(DailyExpense expense) {
        return repository.save(expense);
    }

    public DailyExpense create(DailyExpense dailyExpense) {
//        if (dailyExpense.getId()!= null) {
//            dailyExpense.setDueAmount(data.getPurchaseAmount().subtract(data.getPaidAmount()));
//        }
        BigDecimal totalDeposit = repository.totalDeposit();
        BigDecimal totalExpense = repository.totalExpense();
        BigDecimal totalPaid = repository.totalPaid();

        BigDecimal openingBalance =
                totalDeposit.subtract(totalPaid).subtract(totalExpense);

        dailyExpense.setOpeningBalance(openingBalance);

        BigDecimal closingBalance =
                openingBalance
                        .add(dailyExpense.getCashIn())
                        .subtract(dailyExpense.getCashOut())
                        .subtract(dailyExpense.getExpenseAmount());

        dailyExpense.setClosingBalance(closingBalance);

        BigDecimal previousRunning =
                repository.findTopByOrderByDateDescIdDesc()
                        .map(DailyExpense::getRunningBalance)
                        .orElse(BigDecimal.ZERO);

        BigDecimal running =
                previousRunning
                        .add(dailyExpense.getCashIn())
                        .subtract(dailyExpense.getCashOut());

        dailyExpense.setRunningBalance(running);

        return repository.save(dailyExpense);
    }
    // Summary for frontend header
//    public Map<String, BigDecimal> getSummary() {
//        BigDecimal totalDeposit = repository.totalDeposit();
//        BigDecimal totalExpense = repository.totalExpense();
//        BigDecimal totalPaid = repository.totalPaid();
//
//        // handle nulls
//        totalDeposit = totalDeposit != null ? totalDeposit : BigDecimal.ZERO;
//        totalExpense = totalExpense != null ? totalExpense : BigDecimal.ZERO;
//        totalPaid = totalPaid != null ? totalPaid : BigDecimal.ZERO;
//
//        // calculate opening and closing balances
//        BigDecimal openingBalance = repository.findFirstOpeningBalance(); // first row openingBalance
//        BigDecimal closingBalance = repository.findLastRunningBalance(); // last row runningBalance
//
//        return Map.of(
//                "openingBalance", openingBalance,
//                "closingBalance", closingBalance,
//                "totalDeposit", totalDeposit,
//                "totalExpense", totalExpense,
//                "totalPaid", totalPaid
//        );
//    }
    public BigDecimal getTotalDeposit() {
        return repository.totalDeposit();
    }

    public BigDecimal getTotalExpense() {
        return repository.totalExpense();
    }

    public BigDecimal getTotalPaid() {
        return repository.totalPaid();
    }

    public BigDecimal getFirstOpeningBalance() {
        return repository.findFirstOpeningBalance().orElse(BigDecimal.ZERO);
    }

    public BigDecimal getLastRunningBalance() {
        return repository.findLastRunningBalance().orElse(BigDecimal.ZERO);
    }


    public List<DailyExpense> getAll() {
        return repository.findAll();
    }

    public DailyExpense getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("DailyExpense not found"));
    }

    public List<DailyExpense> getByDate(LocalDate date) {
        return repository.findByDate(date);
    }

    public DailyExpense update(Long id, DailyExpense updated) {
        DailyExpense existing = getById(id);

        existing.setDate(updated.getDate());
        existing.setName(updated.getName());
        existing.setExpenseHead(updated.getExpenseHead());
        existing.setExpenseAmount(updated.getExpenseAmount());
        existing.setCashIn(updated.getCashIn());
        existing.setCashOut(updated.getCashOut());
//        existing.setRunningBalance(updated.getRunningBalance());
        existing.setRemarks(updated.getRemarks());

        BigDecimal opening =
                repository.findPreviousClosingBalance(existing.getDate());

        existing.setOpeningBalance(opening);

        BigDecimal closing =
                opening
                        .add(existing.getCashIn())
                        .subtract(existing.getCashOut())
                        .subtract(existing.getExpenseAmount());

        existing.setClosingBalance(closing);
//        existing.setRunningBalance(closing);


//        DailyExpense existing = repository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Not found"));

        Optional<DailyExpense> last =
                repository.findTopByOrderByDateDescIdDesc();

        boolean isLast = last.isPresent() && last.get().getId().equals(id);

//        existing.setCashIn(updated.getCashIn());
//        existing.setCashOut(updated.getCashOut());
//        existing.setRemarks(updated.getRemarks());

        if (isLast) {
            BigDecimal previousRunning =
                    repository.findTopByOrderByDateDescIdDesc()
                            .filter(d -> !d.getId().equals(id))
                            .map(DailyExpense::getRunningBalance)
                            .orElse(BigDecimal.ZERO);

            existing.setRunningBalance(
                    previousRunning
                            .add(existing.getCashIn())
                            .subtract(existing.getCashOut())
            );

            return repository.save(existing);

        }
           // Otherwise fall through
        repository.save(existing);
        recalcForward(existing.getDate());

        return existing;
    }
    @Transactional
    public void recalcForward(LocalDate fromDate) {

        List<DailyExpense> list =
                repository.findByDateGreaterThanEqualOrderByDateAscIdAsc(fromDate);

        Optional<DailyExpense> prev =
                repository.findTopByDateLessThanEqualAndIdLessThanOrderByDateDescIdDesc(
                        fromDate, Long.MAX_VALUE
                );

        BigDecimal running =
                prev.map(DailyExpense::getRunningBalance)
                        .orElse(BigDecimal.ZERO);

        for (DailyExpense d : list) {
            running = running
                    .add(d.getCashIn())
                    .subtract(d.getCashOut());

            d.setRunningBalance(running);
        }

        repository.saveAll(list);
    }


    @Transactional
    public void delete(Long id) {

        DailyExpense d = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Not found"));

        repository.delete(d);

        recalcForward(d.getDate());
    }

}

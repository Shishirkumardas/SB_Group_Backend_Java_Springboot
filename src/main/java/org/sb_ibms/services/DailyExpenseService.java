package org.sb_ibms.services;

import jakarta.transaction.Transactional;
import org.sb_ibms.models.DailyExpense;
import org.sb_ibms.repositories.DailyExpenseRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
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

        Optional<DailyExpense> firstExpenseOpt = repository.findFirstByOrderByIdAsc();
        return firstExpenseOpt.map(DailyExpense::getOpeningBalance).orElse(BigDecimal.ZERO);
    }

    public BigDecimal getLastRunningBalance() {

        Optional<DailyExpense> lastExpenseOpt = repository.findFirstByOrderByIdDesc();
        return lastExpenseOpt.map(DailyExpense::getRunningBalance).orElse(BigDecimal.ZERO);
    }


    public DailyExpense getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("DailyExpense not found"));
    }

    public List<DailyExpense> getByDate(LocalDate date) {
        return repository.findByDate(date);
    }
    @Transactional
    public DailyExpense update(Long id, DailyExpense updated) {
        DailyExpense existing = getById(id);

        existing.setDate(updated.getDate());
        existing.setName(updated.getName());
        existing.setExpenseHead(updated.getExpenseHead());
        existing.setExpenseAmount(updated.getExpenseAmount());
        existing.setCashIn(updated.getCashIn());
        existing.setCashOut(updated.getCashOut());
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

        Optional<DailyExpense> last =
                repository.findTopByOrderByDateDescIdDesc();

        boolean isLast = last.isPresent() && last.get().getId().equals(id);

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

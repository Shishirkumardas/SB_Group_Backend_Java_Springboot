package org.example.sbgroup2.controller;

import lombok.RequiredArgsConstructor;
import org.example.sbgroup2.models.DailyExpense;
import org.example.sbgroup2.models.MasterData;
import org.example.sbgroup2.repositories.DailyExpenseRepository;
import org.example.sbgroup2.services.DailyExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/daily-expenses")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3001")

public class DailyExpenseController {

    @Autowired
    private final DailyExpenseService service;
    private final DailyExpenseRepository repository;


    //    public DailyExpenseController(DailyExpenseService service) {
//        this.service = service;
//    }
@GetMapping
public List<DailyExpense> getAll() {
    return repository.findAll();
}
    @GetMapping("/dailyExpenses")
    public DailyExpense getMasterData(@RequestParam(required = false) Long id,
                                    @RequestParam(required = false) String name) {
        if (id != null) {
            return service.getById(id);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Either id or name must be provided");
        }
    }

//    // ✅ Summary endpoint
//    @GetMapping("/summary")
//    public Map<String, BigDecimal> getSummary() {
//        return service.getSummary();
//    }
@GetMapping("/summary")
public Map<String, BigDecimal> getSummary() {
    BigDecimal openingBalance = service.getFirstOpeningBalance();
    BigDecimal closingBalance = service.getLastRunningBalance();
    BigDecimal totalDeposit = service.getTotalDeposit();
    BigDecimal totalExpense = service.getTotalExpense();
    BigDecimal totalPaid = service.getTotalPaid();

    return Map.of(
            "openingBalance", openingBalance,
            "closingBalance", closingBalance,
            "totalDeposit", totalDeposit,
            "totalExpense", totalExpense,
            "totalPaid", totalPaid
    );
}


    @PostMapping
    public DailyExpense create(@RequestBody DailyExpense expense) {
        return service.create(expense);
    }

    @GetMapping("/{id}")
    public DailyExpense getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping("/by-date/{date}")
    public List<DailyExpense> getByDate(@PathVariable LocalDate date) {
        return service.getByDate(date);
    }

    @PutMapping("/{id}")
    public DailyExpense update(
            @PathVariable Long id,
            @RequestBody DailyExpense expense
    ) {
        return service.update(id, expense);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}

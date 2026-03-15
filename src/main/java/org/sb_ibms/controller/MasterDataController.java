package org.sb_ibms.controller;

import lombok.RequiredArgsConstructor;
import org.sb_ibms.dto.OverallSummary;
import org.sb_ibms.dto.PaymentView;
import org.sb_ibms.dto.PurchaseView;
import org.sb_ibms.models.MasterData;
import org.sb_ibms.repositories.CashbackPaymentRepository;
import org.sb_ibms.repositories.MasterDataRepository;
import org.sb_ibms.services.MasterDataService;
import org.springframework.http.HttpStatus;

import org.springframework.web.bind.annotation.*;

import org.springframework.web.server.ResponseStatusException;

import java.util.List;
@RestController
@RequestMapping("/api/master-data")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3001")
public class MasterDataController {

    private final MasterDataRepository repo;
    private final MasterDataService masterDataService;
    private final CashbackPaymentRepository cashbackPaymentRepository;

    @GetMapping
    public List<MasterData> getAll() {
        return repo.findAll();
    }

    @GetMapping("/masterData")
    public MasterData getMasterData(@RequestParam(required = false) Long id,
                                    @RequestParam(required = false) String name) {
        if (id != null) {
            return masterDataService.getMasterDataById(id);
        } else if (name != null) {
            return masterDataService.getMasterDataByName(name);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Either id or name must be provided");
        }
    }

    @PutMapping("/{id}")
    public MasterData updateMasterData(@PathVariable Long id, @RequestBody MasterData masterData) {
        return masterDataService.updateMasterData(id, masterData);
    }


    @PostMapping
    public MasterData create(@RequestBody MasterData masterData) {
        return masterDataService.create(masterData);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        masterDataService.delete(id);
    }
    @DeleteMapping
    public void deleteAll() {
        cashbackPaymentRepository.deleteAll();
        repo.deleteAll();
    }


    @GetMapping("/payments")
    public List<PaymentView> payments() {
        return repo.getPayments();
    }

    @GetMapping("/purchases")
    public List<PurchaseView> purchases() {
        return repo.getPurchases();
    }

    @GetMapping("/summary")
    public OverallSummary summary() {
        return repo.getSummary();
    }


}

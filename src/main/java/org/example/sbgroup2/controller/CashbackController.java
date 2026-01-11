package org.example.sbgroup2.controller;

import lombok.RequiredArgsConstructor;
import org.example.sbgroup2.dto.CashbackDetailsDTO;
import org.example.sbgroup2.models.CashbackPayment;
import org.example.sbgroup2.models.MasterData;
import org.example.sbgroup2.repositories.CashbackPaymentRepository;
import org.example.sbgroup2.repositories.MasterDataRepository;
import org.example.sbgroup2.services.CashbackService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cashback")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3001")
public class CashbackController {

    private final MasterDataRepository masterRepo;
    private final CashbackService cashbackService;
    private final CashbackPaymentRepository paymentRepo;

    @GetMapping("/{masterId}")
    public CashbackDetailsDTO getDetails(@PathVariable Long masterId) {
        MasterData master = masterRepo.findById(masterId).orElseThrow();
        return cashbackService.calculateCashback2(master);
    }

    @PostMapping("/{masterId}/pay")
    public void payCashback(
            @PathVariable Long masterId,
            @RequestBody CashbackPayment payment
    ) {
        MasterData master = masterRepo.findById(masterId).orElseThrow();
        payment.setMasterData(master);
        paymentRepo.save(payment);
    }

    @GetMapping("/{masterId}/payments")
    public List<CashbackPayment> payments(@PathVariable Long masterId) {
        return paymentRepo.findByMasterDataId(masterId);
    }
}



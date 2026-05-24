package org.sb_ibms.controller;

import lombok.RequiredArgsConstructor;
import org.sb_ibms.dto.CashbackDetailsDTO;
import org.sb_ibms.models.ShoppingMallCashbackPayment;
import org.sb_ibms.models.ShoppingMallCustomer;
import org.sb_ibms.repositories.ShoppingMallCahbackPaymentRepository;
import org.sb_ibms.repositories.ShoppingMallCustomerRepository;
import org.sb_ibms.services.ShoppingMallCashbackService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shoppingmall-cashback")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3001")
public class ShoppingMallCashbackController {

    private final ShoppingMallCustomerRepository masterRepo;
    private final ShoppingMallCashbackService cashbackService;
    private final ShoppingMallCahbackPaymentRepository paymentRepo;

    @GetMapping("/{masterId}")
    public CashbackDetailsDTO getDetails(@PathVariable Long masterId) {
        ShoppingMallCustomer master = masterRepo.findById(masterId).orElseThrow();
        return cashbackService.calculateCashback2(master);
    }

    @PostMapping("/{masterId}/pay")
    public void payCashback(
            @PathVariable Long masterId,
            @RequestBody ShoppingMallCashbackPayment payment
    ) {
        ShoppingMallCustomer master = masterRepo.findById(masterId).orElseThrow();
        payment.setMasterData(master);
        paymentRepo.save(payment);
    }

    @GetMapping("/{masterId}/payments")
    public List<ShoppingMallCashbackPayment> payments(@PathVariable Long masterId) {
        return paymentRepo.findByMasterDataId(masterId);
    }
}

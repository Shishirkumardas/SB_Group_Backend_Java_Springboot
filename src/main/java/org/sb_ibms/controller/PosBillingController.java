package org.sb_ibms.controller;

import lombok.RequiredArgsConstructor;
import org.sb_ibms.dto.BillingRequest;
import org.sb_ibms.dto.BillingResponse;
import org.sb_ibms.models.ShoppingMallCustomer;
import org.sb_ibms.models.ShoppingMallProduct;
import org.sb_ibms.services.ShoppingMallPosBillingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/pos")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3001", allowCredentials = "true")
public class PosBillingController {

    private final ShoppingMallPosBillingService posService;

    @PostMapping("/scan")
    public ResponseEntity<ShoppingMallProduct> scanBarcode(@RequestParam String barcode) {
        return ResponseEntity.ok(posService.findByBarcode(barcode));
    }
    @GetMapping("/product/name")
    public ResponseEntity<ShoppingMallProduct> findByName(
            @RequestParam String name
    ) {
        return ResponseEntity.ok(posService.findByName(name));
    }

    @GetMapping("/product/id")
    public ResponseEntity<ShoppingMallProduct> findById(
            @RequestParam Long id
    ) {
        return ResponseEntity.ok(posService.findById(id));
    }

    @PostMapping("/billing")
    public ResponseEntity<BillingResponse> createBill(@RequestBody BillingRequest request) {
        return ResponseEntity.ok(posService.createBill(request));
    }

    @GetMapping("/customer/search")
    public ResponseEntity<ShoppingMallCustomer> findCustomerByPhone(@RequestParam String phone) {
        BigDecimal phoneNumber = new BigDecimal(phone.trim());
        return ResponseEntity.ok(posService.findCustomerByPhone(phoneNumber));
    }
}

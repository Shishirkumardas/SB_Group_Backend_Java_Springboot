package org.sb_ibms.controller;

import lombok.AllArgsConstructor;
import org.sb_ibms.dto.ShoppingMallCustomerFormDTO;
import org.sb_ibms.enums.PaymentMethod;
import org.sb_ibms.enums.ShoppingMallPaymentMethod;
import org.sb_ibms.models.ShoppingMallCustomer;
import org.sb_ibms.services.BkashPaymentService;
import org.sb_ibms.services.ShoppingMallService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shopping-mall-customer")
@CrossOrigin(origins = "http://localhost:3001")
@AllArgsConstructor
public class ShoppingMallCustomerController {
    private final ShoppingMallService shoppingMallService;
    private BkashPaymentService bkashPaymentService;

    @PostMapping("/submit")
//    @PreAuthorize("permitAll()")
    public ResponseEntity<?> submitCustomerForm(
            @RequestBody ShoppingMallCustomerFormDTO dto
    ) {
        return ResponseEntity.ok(shoppingMallService.saveCustomerForm(dto));
    }

    @PostMapping("/pay")
    public ResponseEntity<?> pay(
            @RequestParam Long customerId) {

        return ResponseEntity.ok(
                bkashPaymentService.makePayment(customerId)
        );
    }

    @GetMapping("/payment-methods")
    public ResponseEntity<List<String>> getAllPaymentMethods() {
        List<String> methods = ShoppingMallPaymentMethod.getAllMethods();
        return ResponseEntity.ok(methods);
    }

    // ==================== NEW SEARCH ENDPOINT ====================
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'SHOPPING_MALL_MANAGER', 'SHOPPING_MALL_ASSISTANT')")
    public ResponseEntity<List<ShoppingMallCustomer>> searchCustomers(@RequestParam String query) {
        List<ShoppingMallCustomer> customers = shoppingMallService.searchCustomers(query);
        return ResponseEntity.ok(customers);
    }
}

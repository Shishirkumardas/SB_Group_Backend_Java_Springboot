package org.sb_ibms.controller;

import lombok.AllArgsConstructor;
import org.sb_ibms.dto.CustomerFormDTO;
import org.sb_ibms.enums.PaymentMethod;
import org.sb_ibms.services.BkashPaymentService;
import org.sb_ibms.services.MasterDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/customer")
@CrossOrigin(origins = "http://localhost:3001")
@AllArgsConstructor
public class CustomerController {

    private final MasterDataService masterDataService;
    private BkashPaymentService bkashPaymentService;

    @PostMapping("/submit")
    public ResponseEntity<?> submitCustomerForm(
            @RequestBody CustomerFormDTO dto
    ) {
        return ResponseEntity.ok(masterDataService.saveCustomerForm(dto));
    }

    @PostMapping("/pay")
    public ResponseEntity<?> pay(
            @RequestParam Long masterDataId) {

        return ResponseEntity.ok(
                bkashPaymentService.makePayment(masterDataId)
        );
    }

    @GetMapping("/payment-methods")
    public ResponseEntity<List<String>> getAllPaymentMethods() {
        List<String> methods = PaymentMethod.getAllMethods();
        return ResponseEntity.ok(methods);
    }

}


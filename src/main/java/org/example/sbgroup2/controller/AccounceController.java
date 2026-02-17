package org.example.sbgroup2.controller;

import lombok.RequiredArgsConstructor;
import org.example.sbgroup2.dto.AccounceFormDTO;


import org.example.sbgroup2.services.DailyExpenseService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3001")
public class AccounceController {

    private final DailyExpenseService dailyExpenseService;

    @PostMapping("/submit")
    public ResponseEntity<?> submitAccounceForm(
            @RequestBody AccounceFormDTO dto
    ) {
        return ResponseEntity.ok(dailyExpenseService.saveAccounceForm(dto));
    }
//
//    @GetMapping("/payment-methods")
//    public ResponseEntity<List<String>> getAllPaymentMethods() {
//        List<String> methods = PaymentMethod.getAllMethods();
//        return ResponseEntity.ok(methods);
//    }

}

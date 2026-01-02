package org.example.sbgroup2.controller;

import lombok.RequiredArgsConstructor;
import org.example.sbgroup2.dto.PaymentRequest;
import org.example.sbgroup2.dto.PaymentView;
import org.example.sbgroup2.models.Payment;
import org.example.sbgroup2.repositories.MasterDataRepository;
import org.example.sbgroup2.repositories.PaymentRepository;
import org.example.sbgroup2.services.PaymentService;
import org.example.sbgroup2.services.PaymentService2;
import org.example.sbgroup2.services.PaymentServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3001")
public class PaymentController {

    private final PaymentRepository repo;
    private final PaymentService2 paymentService2;
    private final PaymentService paymentService;
    private final MasterDataRepository masterDataRepository;
    private final PaymentServiceImpl paymentServiceImpl;

//    @GetMapping
//    public List<Payment> getAll() {
//        return repo.findAll();
//    }
//
//    @GetMapping("/{id}")
//    public Payment getPaymentById(@PathVariable Long id) {
//        return paymentService.getPaymentById(id);
//    }

    @GetMapping
    public List<PaymentView> payments() {
        return masterDataRepository.getPayments();
    }


    @PostMapping
    public Payment createPayment(@RequestBody Payment payment) {
        return paymentService.createPayment(payment);
    }

    @PutMapping("/{id}")
    public Payment updatePayment(@PathVariable Long id, @RequestBody Payment payment) {
        return paymentService.updatePayment(id, payment);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePayment(@PathVariable Long id) {
        paymentService.deletePayment(id);
        return ResponseEntity.ok().body("Payment deleted successfully");
    }
    @PostMapping("/{id}/pay")
    public Payment addPayment(
            @PathVariable Long id,
            @RequestBody PaymentRequest request
    ) {
        return paymentService2.addPayment(id, request);
    }
    @PostMapping("/customer/{masterId}/pay")
    public Payment completePayment(
            @PathVariable Long masterId
    ) {
        return paymentService2.processPayment(masterId);
    }


    @GetMapping("/{id}/payments")
    public List<Payment> getPayments(@PathVariable Long id) {
        return paymentService2.getPayments(id);
    }

}


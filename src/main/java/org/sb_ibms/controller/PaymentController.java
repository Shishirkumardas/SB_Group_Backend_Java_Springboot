package org.sb_ibms.controller;

import lombok.RequiredArgsConstructor;
import org.sb_ibms.dto.PaymentRequest;
import org.sb_ibms.dto.PaymentView;
import org.sb_ibms.models.Payment;
import org.sb_ibms.repositories.MasterDataRepository;
import org.sb_ibms.services.PaymentService;
import org.sb_ibms.services.PaymentService2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3001")
public class PaymentController {

    private final PaymentService2 paymentService2;
    private final PaymentService paymentService;
    private final MasterDataRepository masterDataRepository;



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


    @GetMapping("/payment/success")
    public String success(@RequestParam String merchantInvoiceNumber) {
        paymentService.markPaymentSuccess(merchantInvoiceNumber);
        return "Payment Successful";
    }

    @GetMapping("/payment/failed")
    public String failed(@RequestParam String merchantInvoiceNumber) {
        paymentService.markPaymentFailed(merchantInvoiceNumber);
        return "Payment Failed";
    }
}


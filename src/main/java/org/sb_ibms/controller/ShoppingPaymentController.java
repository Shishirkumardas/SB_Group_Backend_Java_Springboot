package org.sb_ibms.controller;

import lombok.RequiredArgsConstructor;
import org.sb_ibms.dto.ShoppingMallPaymentRequest;
import org.sb_ibms.dto.ShoppingMallPaymentView;
import org.sb_ibms.models.Payment;
import org.sb_ibms.models.ShoppingMallPayments;
import org.sb_ibms.repositories.ShoppingMallCustomerRepository;
import org.sb_ibms.services.ShoppingMallContext;
import org.sb_ibms.services.ShoppingMallPaymentInterface;
import org.sb_ibms.services.ShoppingMallPaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shoppingMall-payments")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3001")
public class ShoppingPaymentController {
    private final ShoppingMallPaymentInterface paymentService2;
    private final ShoppingMallPaymentService paymentService;
    private final ShoppingMallCustomerRepository masterDataRepository;
    private final ShoppingMallContext shoppingMallContext;



//    @GetMapping
//    public List<ShoppingMallPaymentView> payments() {
//        return masterDataRepository.getPayments();
//    }
//
//
//    @PostMapping
//    public ShoppingMallPayments createPayment(@RequestBody ShoppingMallPayments payment) {
//        return paymentService.createPayment(payment);
//    }

    @GetMapping
    public List<ShoppingMallPaymentView> payments() {
        Long mallId = shoppingMallContext.getCurrentMallId();
        return masterDataRepository.getPayments(mallId);
    }

    @PostMapping
    public ShoppingMallPayments createPayment(@RequestBody ShoppingMallPayments payment) {
        Long mallId = shoppingMallContext.getCurrentMallId();
        payment.setShoppingMallId(mallId);
        return paymentService.createPayment(payment);
    }

    @PutMapping("/{id}")
    public ShoppingMallPayments updatePayment(@PathVariable Long id, @RequestBody Payment payment) {
        return paymentService.updatePayment(id, payment);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePayment(@PathVariable Long id) {
        paymentService.deletePayment(id);
        return ResponseEntity.ok().body("Payment deleted successfully");
    }
    @PostMapping("/{id}/pay")
    public ShoppingMallPayments addPayment(
            @PathVariable Long id,
            @RequestBody ShoppingMallPaymentRequest request
    ) {
        return paymentService2.addPayment(id, request);
    }

    // Add these inside ShoppingPaymentController class

    @GetMapping("/customer/{customerId}")
    public List<ShoppingMallPayments> getCustomerPayments(@PathVariable Long customerId) {
        return paymentService2.getPayments(customerId);
    }


    @GetMapping("/customer/{customerId}/summary")
    public ResponseEntity<?> getCustomerPaymentSummary(@PathVariable Long customerId) {
        // You can implement this in service if needed
        return ResponseEntity.ok(paymentService2.getPayments(customerId));
    }

    @PostMapping("/customer/{masterId}/pay")
    public ShoppingMallPayments completePayment(
            @PathVariable Long masterId
    ) {
        return paymentService2.processPayment(masterId);
    }


    @GetMapping("/{id}/payments")
    public List<ShoppingMallPayments> getPayments(@PathVariable Long id) {
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

package org.sb_ibms.controller;

import org.sb_ibms.models.MasterData;
import org.sb_ibms.repositories.MasterDataRepository;
import org.sb_ibms.services.BkashPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/bkash")
public class BkashController {

    @Autowired
    private BkashPaymentService paymentService;

    @Autowired
    private MasterDataRepository masterDataRepository;

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestParam BigDecimal amount) {

        String invoice = "INV-" + System.currentTimeMillis();
        Map<String, Object> response = paymentService.createPayment(amount, invoice);

        return ResponseEntity.ok(response);
    }
    @Autowired
    private BkashPaymentService bkashPaymentService;


    @PostMapping("/pay")
    public ResponseEntity<?> pay(
            @RequestParam Long masterDataId) {

        return ResponseEntity.ok(
                bkashPaymentService.makePayment(masterDataId)
        );
    }

    @GetMapping("/search")

    public ResponseEntity<?> searchTransaction(
            @RequestParam String trxId) {

        try {
            Map<String, Object> result = paymentService.searchTransaction(trxId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", true,
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/callback")
    public ResponseEntity<?> callback(
            @RequestParam String masterDataId,
            @RequestParam String paymentID,
            @RequestParam String status,
            @RequestParam(required = false) String signature,
            @RequestParam(required = false) String apiVersion) {

        System.out.println("Callback Received: masterDataId=" + masterDataId +
                ", PaymentID=" + paymentID + ", Status=" + status);


        MasterData md = masterDataRepository.getById(Long.valueOf(masterDataId));

        if (!"success".equalsIgnoreCase(status)) {

            md.setPaymentCompleted(false);
            masterDataRepository.save(md);

            return ResponseEntity.badRequest().body("Payment Failed: " + status);
        }

        try {

            Map<String, Object> queryResult = paymentService.queryPayment(paymentID);

            String trxStatus = (String) queryResult.get("transactionStatus");

            if ("Completed".equals(trxStatus)) {
                System.out.println("Payment already completed - using query result");
                md.setPaymentCompleted(true);
                masterDataRepository.save(md);
                return ResponseEntity.ok(queryResult);
            }


            Map<String, Object> executeResult = paymentService.executePayment(paymentID);

            md.setPaymentCompleted(true);
            masterDataRepository.save(md);
            return ResponseEntity.ok(executeResult);

        } catch (RuntimeException e) {
            String msg = e.getMessage();


            if (msg.contains("payment_already_completed") ||
                    msg.contains("2062") ||
                    msg.contains("The payment has already been completed")) {

                System.out.println("Detected already completed payment");
                md.setPaymentCompleted(true);
                masterDataRepository.save(md);
                return ResponseEntity.ok(Map.of(
                        "paymentId", paymentID,
                        "transactionStatus", "Completed",
                        "message", "Payment already completed successfully"
                ));
            }

            md.setPaymentCompleted(false);
            masterDataRepository.save(md);
            return ResponseEntity.badRequest().body("Payment processing error: " + msg);
        }
    }

}


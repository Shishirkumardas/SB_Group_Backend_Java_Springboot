package org.example.sbgroup2.controller;

import org.example.sbgroup2.models.MasterData;
import org.example.sbgroup2.repositories.MasterDataRepository;
import org.example.sbgroup2.services.BkashPaymentService;
import org.example.sbgroup2.services.MasterDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/bkash")
public class BkashController {

    @Autowired
    private BkashPaymentService paymentService;
    @Autowired
    private MasterDataService masterDataService;
    @Autowired
    private MasterDataRepository masterDataRepository;

    // This will be used for random payment not for any id, customer,masterData
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestParam BigDecimal amount) {

        String invoice = "INV-" + System.currentTimeMillis();
        Map<String, Object> response = paymentService.createPayment(amount, invoice);

        return ResponseEntity.ok(response);
    }
    @Autowired
    private BkashPaymentService bkashPaymentService;

    // This will be used for payments for each customer id, masterDataID, cashbackID
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

//    @GetMapping("/callback")
//    public ResponseEntity<?> callback(
//            @RequestParam String paymentID,
//            @RequestParam String status,
//            @RequestParam(required = false) String signature,
//            @RequestParam(required = false) String apiVersion) {
//
//        System.out.println("Callback Received:");
//        System.out.println("PaymentID = " + paymentID);
//        System.out.println("Status = " + status);
//        System.out.println("Signature = " + signature);
//
//        if ("success".equalsIgnoreCase(status)) {
//            Map result = paymentService.executePayment(paymentID);
//            return ResponseEntity.ok(result);
//        }
//
//        return ResponseEntity.badRequest().body("Payment Failed: " + status);
//    }

    @GetMapping("/callback")
    public ResponseEntity<?> callback(
            @RequestParam String masterDataId,
            @RequestParam String paymentID,
            @RequestParam String status,
            @RequestParam(required = false) String signature,
            @RequestParam(required = false) String apiVersion) {

        System.out.println("Callback Received: masterDataId=" + masterDataId +
                ", PaymentID=" + paymentID + ", Status=" + status);

        // Load the record early (but don't modify yet)
        MasterData md = masterDataRepository.getById(Long.valueOf(masterDataId));

        if (!"success".equalsIgnoreCase(status)) {
            // For failure/cancel → do NOT set paymentCompleted = true
            // Optionally log or update status to FAILED/CANCELLED
            md.setPaymentCompleted(false); // ensure it's false (in case it was true before)
            masterDataRepository.save(md); // persist the false state if needed

            return ResponseEntity.badRequest().body("Payment Failed: " + status);
        }

        try {
            // Step 1: First try to query current status (safest & idempotent)
            Map<String, Object> queryResult = paymentService.queryPayment(paymentID);

            String trxStatus = (String) queryResult.get("transactionStatus");

            if ("Completed".equals(trxStatus)) {
                // Already completed → success
                System.out.println("Payment already completed - using query result");
                md.setPaymentCompleted(true);
                masterDataRepository.save(md); // update DB
                return ResponseEntity.ok(queryResult);
            }

            // Step 2: If not completed → try execute
            Map<String, Object> executeResult = paymentService.executePayment(paymentID);

            // If execute succeeds → mark as completed
            md.setPaymentCompleted(true);
            masterDataRepository.save(md);
            return ResponseEntity.ok(executeResult);

        } catch (RuntimeException e) {
            String msg = e.getMessage();

            // Handle the specific "already completed" case as SUCCESS
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

            // Any OTHER error → payment is NOT completed
            md.setPaymentCompleted(false);
            masterDataRepository.save(md); // make sure it's false
            return ResponseEntity.badRequest().body("Payment processing error: " + msg);
        }
    }

}


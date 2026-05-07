package org.sb_ibms.controller;

import lombok.RequiredArgsConstructor;
import org.sb_ibms.BarcodeGenerator;
import org.sb_ibms.dto.RewardCardDTO;
import org.sb_ibms.models.RewardCard;
import org.sb_ibms.models.RewardTransaction;
import org.sb_ibms.services.RewardService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rewards")
@RequiredArgsConstructor
@CrossOrigin(
        origins = "${frontend.origin:http://localhost:3001}",
        allowCredentials = "true",
        allowedHeaders = "*",
        exposedHeaders = "Set-Cookie"
)
public class RewardController {
    private final RewardService rewardService;

    /**
     * Issue Reward Card to a Shopping Mall Customer
     */
    @PostMapping("/issue")
    @PreAuthorize("hasAnyRole('ADMIN', 'SHOPPING_MALL_MANAGER')")
    public ResponseEntity<RewardCard> issueRewardCard(@RequestParam String customerId) {
        RewardCard card = rewardService.issueRewardCard(customerId);
        return ResponseEntity.ok(card);
    }


    // Add this method in RewardController.java
    @GetMapping("/{cardId}/barcode")
    @PreAuthorize("hasAnyRole('ADMIN', 'SHOPPING_MALL_MANAGER', 'SHOPPING_MALL_ASSISTANT')")
    public ResponseEntity<byte[]> generateBarcode(@PathVariable String cardId) {
        try {
            RewardCard card = rewardService.getRewardCardById(cardId);

            // Using ZXing library (recommended)
            BarcodeGenerator generator = new BarcodeGenerator();
            byte[] barcodeImage = generator.generateBarcode(card.getCardNumber(), 400, 150);

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .header("Content-Disposition", "inline; filename=\"barcode-" + cardId + ".png\"")
                    .body(barcodeImage);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Add Points to a Reward Card
     */
    @PostMapping("/add-points")
    @PreAuthorize("hasAnyRole('ADMIN', 'SHOPPING_MALL_MANAGER', 'SHOPPING_MALL_ASSISTANT')")
    public ResponseEntity<Void> addPoints(
            @RequestParam String cardId,
            @RequestParam int points,
            @RequestParam String reason) {

        rewardService.addPoints(cardId, points, reason);
        return ResponseEntity.ok().build();
    }

    /**
     * Redeem Points for Cashback
     */
    @PostMapping("/redeem")
    @PreAuthorize("hasAnyRole('ADMIN', 'SHOPPING_MALL_MANAGER', 'SHOPPING_MALL_CUSTOMER')")
    public ResponseEntity<Void> redeemPoints(
            @RequestParam String cardId,
            @RequestParam int pointsToRedeem,
            @RequestParam(required = false) String remarks) {

        rewardService.redeemPointsForCashback(cardId, pointsToRedeem, remarks);
        return ResponseEntity.ok().build();
    }

    /**
     * Get Reward Card by Customer ID
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<RewardCard> getRewardCardByCustomer(@PathVariable String customerId) {
        RewardCard card = rewardService.getRewardCardByCustomer(customerId);
        return ResponseEntity.ok(card);
    }

    /**
     * Get Reward Card by Card ID
     */
    @GetMapping("/{cardId}")
    public ResponseEntity<RewardCard> getRewardCard(@PathVariable String cardId) {
        RewardCard card = rewardService.getRewardCardById(cardId);
        return ResponseEntity.ok(card);
    }

//    @GetMapping("/all")
//    @PreAuthorize("hasAnyRole('ADMIN', 'SHOPPING_MALL_MANAGER', 'SHOPPING_MALL_ASSISTANT')")
//    public ResponseEntity<List<RewardCard>> getAllRewardCards() {
//        List<RewardCard> cards = rewardService.getAllRewardCards();
//        return ResponseEntity.ok(cards);
//    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'SHOPPING_MALL_MANAGER', 'SHOPPING_MALL_ASSISTANT')")
    public ResponseEntity<List<RewardCard>> getAllRewardCards() {
        List<RewardCard> cards = rewardService.getAllRewardCards();
        return ResponseEntity.ok(cards);
    }

    /**
     * Get Transaction History of a Card
     */
    @GetMapping("/{cardId}/transactions")
    public ResponseEntity<List<RewardTransaction>> getTransactionHistory(@PathVariable String cardId) {
        List<RewardTransaction> transactions = rewardService.getTransactionHistory(cardId);
        return ResponseEntity.ok(transactions);
    }

    /**
     * Deactivate a Reward Card
     */
    @PutMapping("/{cardId}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'SHOPPING_MALL_MANAGER')")
    public ResponseEntity<Void> deactivateCard(@PathVariable String cardId) {
        rewardService.deactivateCard(cardId);
        return ResponseEntity.ok().build();
    }


}

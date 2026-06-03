package org.sb_ibms.controller;

import lombok.RequiredArgsConstructor;
import org.sb_ibms.BarcodeGenerator;
import org.sb_ibms.dto.RedeemRequest;
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

    @PostMapping("/issue")
    @PreAuthorize("hasAnyRole('ADMIN', 'SHOPPING_MALL_MANAGER')")
    public ResponseEntity<RewardCard> issueRewardCard(@RequestParam String customerId) {
        return ResponseEntity.ok(rewardService.issueRewardCard(customerId));
    }

    @GetMapping("/{cardId}/barcode")
    @PreAuthorize("hasAnyRole('ADMIN', 'SHOPPING_MALL_MANAGER', 'SHOPPING_MALL_ASSISTANT')")
    public ResponseEntity<byte[]> generateBarcode(@PathVariable String cardId) {
        try {
            RewardCard card = rewardService.getRewardCardById(cardId);
            BarcodeGenerator generator = new BarcodeGenerator();
            byte[] image = generator.generateBarcode(card.getCardNumber(), 900, 260);

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .header("Content-Disposition", "inline; filename=\"barcode-" + cardId + ".png\"")
                    .body(image);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{cardId}/qr")
    @PreAuthorize("hasAnyRole('ADMIN', 'SHOPPING_MALL_MANAGER', 'SHOPPING_MALL_ASSISTANT')")
    public ResponseEntity<byte[]> generateQRCode(@PathVariable String cardId) {
        try {
            RewardCard card = rewardService.getRewardCardById(cardId);
            BarcodeGenerator generator = new BarcodeGenerator();
            byte[] image = generator.generateQRCode(card.getCardNumber(), 400, 400);

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .header("Content-Disposition", "inline; filename=\"qr-" + cardId + ".png\"")
                    .body(image);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/add-points")
    @PreAuthorize("hasAnyRole('ADMIN', 'SHOPPING_MALL_MANAGER', 'SHOPPING_MALL_ASSISTANT')")
    public ResponseEntity<Void> addPoints(@RequestParam String cardId,
                                          @RequestParam int points,
                                          @RequestParam String reason) {
        rewardService.addPoints(cardId, points, reason);
        return ResponseEntity.ok().build();
    }

//    @PostMapping("/redeem")
//    @PreAuthorize("hasAnyRole('ADMIN', 'SHOPPING_MALL_MANAGER')")
//    public ResponseEntity<Void> redeemPoints(@RequestParam String cardId,
//                                             @RequestParam int pointsToRedeem,
//                                             @RequestParam(required = false) String remarks) {
//        rewardService.redeemPointsForCashback(cardId, pointsToRedeem, remarks);
//        return ResponseEntity.ok().build();
//    }

    @PostMapping("/redeem")
    @PreAuthorize("hasAnyRole('ADMIN', 'SHOPPING_MALL_MANAGER')")
    public ResponseEntity<String> redeemPoints(@RequestBody RedeemRequest request) {
        rewardService.redeemPointsForCashback(
                request.getCardId(),
                request.getPointsToRedeem(),
                request.getRemarks()
        );
        return ResponseEntity.ok("Points redeemed successfully");
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SHOPPING_MALL_MANAGER', 'SHOPPING_MALL_ASSISTANT')")
    public ResponseEntity<RewardCard> getRewardCardByCustomer(@PathVariable String customerId) {
        return ResponseEntity.ok(rewardService.getRewardCardByCustomer(customerId));
    }

    @GetMapping("/number/{cardNumber}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SHOPPING_MALL_MANAGER', 'SHOPPING_MALL_ASSISTANT')")
    public ResponseEntity<RewardCardDTO> getByCardNumber(@PathVariable String cardNumber) {
        RewardCard card = rewardService.getRewardCardByNumber(cardNumber);
        return ResponseEntity.ok(rewardService.getRewardCardByIdDTO(String.valueOf(card.getId())));
    }

    @GetMapping("/{cardId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SHOPPING_MALL_MANAGER', 'SHOPPING_MALL_ASSISTANT')")
    public ResponseEntity<RewardCardDTO> getRewardCard(@PathVariable String cardId) {
        return ResponseEntity.ok(rewardService.getRewardCardByIdDTO(cardId));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'SHOPPING_MALL_MANAGER', 'SHOPPING_MALL_ASSISTANT')")
    public ResponseEntity<List<RewardCardDTO>> getAllRewardCards() {
        return ResponseEntity.ok(rewardService.getAllRewardCards());
    }

//    @GetMapping("/{cardId}/transactions")
//    @PreAuthorize("hasAnyRole('ADMIN', 'SHOPPING_MALL_MANAGER', 'SHOPPING_MALL_ASSISTANT')")
//    public ResponseEntity<List<RewardTransaction>> getTransactionHistory(@PathVariable String cardId) {
//        return ResponseEntity.ok(rewardService.getTransactionHistory(cardId));
//    }

    @PutMapping("/{cardId}/activate")
    @PreAuthorize("hasAnyRole('ADMIN', 'SHOPPING_MALL_MANAGER')")
    public ResponseEntity<Void> activateCard(@PathVariable String cardId) {
        rewardService.activateCard(cardId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{cardId}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'SHOPPING_MALL_MANAGER')")
    public ResponseEntity<Void> deactivateCard(@PathVariable String cardId) {
        rewardService.deactivateCard(cardId);
        return ResponseEntity.ok().build();
    }
}
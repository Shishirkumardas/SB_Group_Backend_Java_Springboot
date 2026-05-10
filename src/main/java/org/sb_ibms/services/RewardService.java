package org.sb_ibms.services;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.sb_ibms.dto.RewardCardDTO;
import org.sb_ibms.enums.Role;
import org.sb_ibms.models.MasterData;
import org.sb_ibms.models.RewardCard;
import org.sb_ibms.models.RewardTransaction;
import org.sb_ibms.models.User;
import org.sb_ibms.repositories.MasterDataRepository;
import org.sb_ibms.repositories.RewardCardRepository;
import org.sb_ibms.repositories.RewardTransactionRepository;
import org.sb_ibms.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class RewardService {
    private final RewardCardRepository rewardCardRepo;
    private final RewardTransactionRepository rewardTransactionRepo;
    private final UserRepository userRepo;
    private final CashbackService cashbackService;
    private final MasterDataRepository masterDataRepository;


    @Transactional
    public RewardCard issueRewardCard(String customerId) {
//        User customer = userRepo.findById(customerId)
//                .orElseThrow(() -> new IllegalArgumentException("Customer not found with ID: " + customerId));

//        if (customer.getRole() != Role.SHOPPING_MALL_CUSTOMER) {
//            throw new IllegalArgumentException("Reward card can only be issued to SHOPPING_MALL_CUSTOMER role");
//        }

        MasterData customer = masterDataRepository.findById(Long.valueOf(customerId))
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with ID: " + customerId));




        if (rewardCardRepo.existsByCustomerId(Long.valueOf(customerId))) {
            throw new IllegalArgumentException("Reward card already exists for this customer");
        }

        RewardCard card = new RewardCard();
        card.setCustomer(customer);
        card.setCardNumber("SBMALL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        card.setTotalPoints(0);
        card.setIssuedAt(LocalDateTime.now());
        card.setActive(true);



        RewardCard savedCard = rewardCardRepo.save(card);


         addTransaction(savedCard, 0, "Reward Card Issued");

        return savedCard;
    }

    @Transactional
    public void addPoints(String cardId, int points, String reason) {
        if (points <= 0) {
            throw new IllegalArgumentException("Points must be greater than 0");
        }
        long id = Long.parseLong(cardId);

        RewardCard card = rewardCardRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reward card not found"));

        if (!card.isActive()) {
            throw new IllegalStateException("Reward card is inactive");
        }

        int previousPoints = card.getTotalPoints();
        card.setTotalPoints(previousPoints + points);
        rewardCardRepo.save(card);

        // Transaction Log
        addTransaction(card, points, reason);
    }

    @Transactional
    public void redeemPointsForCashback(String cardId, int pointsToRedeem, String remarks) {
        long id = Long.parseLong(cardId);
        RewardCard card = rewardCardRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reward card not found"));

        if (!card.isActive()) {
            throw new IllegalStateException("Reward card is inactive");
        }

        if (pointsToRedeem <= 0) {
            throw new IllegalArgumentException("Points to redeem must be greater than 0");
        }

        if (card.getTotalPoints() < pointsToRedeem) {
            throw new IllegalArgumentException("Insufficient points. Available: " + card.getTotalPoints());
        }

        MasterData customer = card.getCustomer();

        //1 point == 1 TK
        double cashbackAmount = pointsToRedeem * 1.0;


        String cashbackId = cashbackService.createAndInitiateShoppingMallCashback(
                String.valueOf(customer.getId()),
                cashbackAmount,
                "Reward Points Redemption: " + pointsToRedeem + " points",
                remarks
        );

        // Point Deduction
        card.setTotalPoints(card.getTotalPoints() - pointsToRedeem);
        rewardCardRepo.save(card);

        // Transaction Log (Negative points = Redeem)
        addTransaction(card, -pointsToRedeem,
                "Redeemed for cashback. Cashback ID: " + cashbackId + " | Amount: " + cashbackAmount + " BDT");
    }



    private void addTransaction(RewardCard card, int points, String description) {
        RewardTransaction tx = new RewardTransaction();
        tx.setRewardCard(card);
        tx.setPointsEarned(points);
        tx.setDescription(description);
        tx.setExpiresAt(LocalDateTime.now().plusMonths(12));
        tx.setTimestamp(LocalDateTime.now());
        // ← THIS WAS MISSING → Caused the error
        tx.setTransactionDate(LocalDateTime.now());
        rewardTransactionRepo.save(tx);
    }
    public RewardCard getRewardCardByCustomer(String customerId) {
        return rewardCardRepo.findByCustomerId(Long.valueOf(customerId))
                .orElseThrow(() -> new IllegalArgumentException("No reward card found for customer"));
    }
    public RewardCard getRewardCardByNumber(
            String cardNumber
    ) {

        return rewardCardRepo
                .findByCardNumber(cardNumber)
                .orElseThrow(() ->
                        new RuntimeException("Card not found"));
    }

    public RewardCard getRewardCardById(String cardId) {
        long id=Long.parseLong(cardId);
        return rewardCardRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reward card not found"));
    }
    public RewardCardDTO getRewardCardByIdDTO(String cardId) {
        long id = Long.parseLong(cardId);
        RewardCard card = rewardCardRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reward card not found"));

        return convertToDTO(card);
    }

//    public List<RewardCard> getAllRewardCards() {
//        return rewardCardRepo.findAll();
//    }

    // Add this method
    public List<RewardCardDTO> getAllRewardCards() {
        List<RewardCard> cards = rewardCardRepo.findAll();
        return cards.stream().map(this::convertToDTO).toList();
    }

    public List<RewardTransaction> getTransactionHistory(String cardId) {
        RewardCard card = getRewardCardById(cardId);
        return rewardTransactionRepo.findByRewardCardOrderByTransactionDateDesc(card);
    }

//    @Transactional
//    public void activateCard(String cardId) {
//        RewardCard card = getRewardCardById(cardId);
//
//        if (!card.isActive()) {           // Only update if needed
//            card.setActive(true);
//            rewardCardRepo.save(card);
//        }
//    }

    @Transactional
    public void activateCard(String cardId) {
        RewardCard card = getRewardCardById(cardId);
        System.out.println("=== BEFORE ACTIVATE === ID: " + card.getId() + " | Active: " + card.isActive()); // Debug

        card.setActive(true);
        RewardCard saved = rewardCardRepo.save(card);

        System.out.println("=== AFTER ACTIVATE === ID: " + saved.getId() + " | Active: " + saved.isActive()); // Debug
    }


    @Transactional
    public void expireOldPoints() {
        // Run daily via scheduler
        List<RewardTransaction> expiredTxs = rewardTransactionRepo.findExpirableTransactions(LocalDateTime.now());

        for (RewardTransaction tx : expiredTxs) {
            if (tx.getPointsEarned() > 0 && tx.isExpired()) {
                RewardCard card = tx.getRewardCard();
                if (card.isActive()) {
                    // Create expiry transaction
                    RewardTransaction expiryTx = new RewardTransaction();
                    expiryTx.setRewardCard(card);
                    expiryTx.setPointsEarned(-tx.getPointsEarned());
                    expiryTx.setDescription("Points Expired: " + tx.getDescription());
                    expiryTx.setTimestamp(LocalDateTime.now());
                    expiryTx.setTransactionDate(LocalDateTime.now());
                    rewardTransactionRepo.save(expiryTx);

                    card.setTotalPoints(card.getTotalPoints() - tx.getPointsEarned());
                }
            }
        }
//        rewardCardRepo.saveAll( expiredTxs);
    }

    private RewardCardDTO convertToDTO(RewardCard card) {
        RewardCardDTO dto = new RewardCardDTO();
        dto.setId(card.getId());
        dto.setCardNumber(card.getCardNumber());
        dto.setTotalPoints(card.getTotalPoints());
        dto.setIssuedAt(card.getIssuedAt());
        dto.setActive(card.isActive());


        if (card.getCustomer() != null) {
            RewardCardDTO.CustomerSummary summary = new RewardCardDTO.CustomerSummary();
            summary.setId(card.getCustomer().getId());
            summary.setName(card.getCustomer().getName());
            summary.setPhone(card.getCustomer().getPhone() != null
                    ? "0"+card.getCustomer().getPhone().toBigInteger() : "");
            dto.setCustomer(summary);
        }

        return dto;
    }

//    @Transactional
//    public void deactivateCard(String cardId) {
//        RewardCard card = getRewardCardById(cardId);
//
//        if (card.isActive()) {            // Fixed condition
//            card.setActive(false);
//            rewardCardRepo.save(card);
//        }
//    }

    @Transactional
    public void deactivateCard(String cardId) {
        RewardCard card = getRewardCardById(cardId);
        System.out.println("=== BEFORE DEACTIVATE === ID: " + card.getId() + " | Active: " + card.isActive());

        card.setActive(false);
        RewardCard saved = rewardCardRepo.save(card);

        System.out.println("=== AFTER DEACTIVATE === ID: " + saved.getId() + " | Active: " + saved.isActive());
    }
}

package org.sb_ibms.services;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.sb_ibms.dto.RewardCardDTO;
import org.sb_ibms.models.RewardCard;
import org.sb_ibms.models.RewardTransaction;
import org.sb_ibms.models.ShoppingMallCustomer;
import org.sb_ibms.repositories.RewardCardRepository;
import org.sb_ibms.repositories.RewardTransactionRepository;
import org.sb_ibms.repositories.ShoppingMallCustomerRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class RewardService {

    private final RewardCardRepository rewardCardRepo;
    private final RewardTransactionRepository rewardTransactionRepo;
    private final ShoppingMallCustomerRepository shoppingMallCustomerRepository;
    private final ShoppingMallContext shoppingMallContext;
    private final SmsNotificationService smsNotificationService;

    @Transactional
    public RewardCard issueRewardCard(String customerId) {
        Long mallId = shoppingMallContext.getCurrentMallId();

        ShoppingMallCustomer customer = shoppingMallCustomerRepository.findById(Long.valueOf(customerId))
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with ID: " + customerId));

        if (mallId != null && !mallId.equals(customer.getShoppingMallId())) {
            throw new RuntimeException("Access denied: This customer belongs to another shopping mall");
        }

        if (rewardCardRepo.existsByCustomerId(Long.valueOf(customerId))) {
            throw new IllegalArgumentException("Reward card already exists for this customer");
        }

        RewardCard card = new RewardCard();
        card.setCustomer(customer);
        card.setCardNumber("SBMALL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        card.setTotalPoints(0);
        card.setIssuedAt(LocalDateTime.now());
        card.setActive(true);
        card.setShoppingMallId(mallId);

        RewardCard savedCard = rewardCardRepo.save(card);

        customer.setRewardCard(savedCard);
        shoppingMallCustomerRepository.save(customer);

        addTransaction(savedCard, 0, "Reward Card Issued");

        try {
            String smsMessage = "Congratulations! Your SB Mall Reward Card has been issued.\n" +
                    "Card No: " + savedCard.getCardNumber() + "\n" +
                    "Start earning points today!";

            smsNotificationService.sendSmsToCustomer(Long.valueOf(customerId), smsMessage);
        } catch (Exception e) {
            System.err.println("Failed to send SMS after reward card issue: " + e.getMessage());
        }

        return savedCard;
    }

    @Transactional
    public void addPoints(String cardNumber, int points, String reason) {
        if (points <= 0) return;

        RewardCard card = rewardCardRepo.findByCardNumber(cardNumber)
                .orElseThrow(() -> new IllegalArgumentException("Reward card not found"));

        Long mallId = shoppingMallContext.getCurrentMallId();
        if (mallId != null && !mallId.equals(card.getShoppingMallId())) {
            throw new RuntimeException("Access denied: This reward card belongs to another shopping mall");
        }

        card.setTotalPoints(card.getTotalPoints() + points);
        rewardCardRepo.save(card);

        addTransaction(card, points, reason);
    }

    @Transactional
    public void redeemPointsForCashback(String cardId, int pointsToRedeem, String remarks) {
        RewardCard card = rewardCardRepo.findById(Long.parseLong(cardId))
                .orElseThrow(() -> new IllegalArgumentException("Reward card not found"));
        String customerId = card.getCustomer() != null ? String.valueOf(card.getCustomer().getId()) : "Unknown";

        Long mallId = shoppingMallContext.getCurrentMallId();
        if (mallId != null && !mallId.equals(card.getShoppingMallId())) {
            throw new RuntimeException("Access denied: This reward card belongs to another shopping mall");
        }

        if (card.getTotalPoints() < pointsToRedeem) {
            throw new IllegalArgumentException("Insufficient points");
        }

        card.setTotalPoints(card.getTotalPoints() - pointsToRedeem);
        rewardCardRepo.save(card);


        addTransaction(card, -pointsToRedeem, "Redeemed for cashback | " + remarks);
        try {

            String smsMessage = "Your SB Mall Reward Card points has been redeemed.\n" +
                    "Card No: " + card.getCardNumber() + "\n" +
                    "Total Points: " + (card.getTotalPoints()) + "\n" +
                    "Thank you for shopping at SB Mall!";

            smsNotificationService.sendSmsToCustomer(Long.valueOf(customerId), smsMessage);
        } catch (Exception e) {
            System.err.println("Failed to send SMS after reward card issue: " + e.getMessage());
        }
    }

    private void addTransaction(RewardCard card, int points, String description) {
        RewardTransaction tx = new RewardTransaction();
        tx.setRewardCard(card);
        tx.setPointsEarned(points);
        tx.setDescription(description);
        tx.setTimestamp(LocalDateTime.now());
        tx.setTransactionDate(LocalDateTime.now());
        tx.setExpiresAt(LocalDateTime.now().plusMonths(12));
        rewardTransactionRepo.save(tx);
    }

    public RewardCard getRewardCardByCustomer(String customerId) {
        RewardCard card = rewardCardRepo.findByCustomerId(Long.valueOf(customerId))
                .orElseThrow(() -> new IllegalArgumentException("No reward card found"));

        Long mallId = shoppingMallContext.getCurrentMallId();
        if (mallId != null && !mallId.equals(card.getShoppingMallId())) {
            throw new RuntimeException("Access denied");
        }
        return card;
    }

    public RewardCard getRewardCardByNumber(String cardNumber) {
        RewardCard card = rewardCardRepo.findByCardNumber(cardNumber)
                .orElseThrow(() -> new RuntimeException("Card not found"));

        Long mallId = shoppingMallContext.getCurrentMallId();
        if (mallId != null && !mallId.equals(card.getShoppingMallId())) {
            throw new RuntimeException("Access denied");
        }
        return card;
    }

    public RewardCard getRewardCardById(String cardId) {
        RewardCard card = rewardCardRepo.findById(Long.parseLong(cardId))
                .orElseThrow(() -> new IllegalArgumentException("Reward card not found"));

        Long mallId = shoppingMallContext.getCurrentMallId();
        if (mallId != null && !mallId.equals(card.getShoppingMallId())) {
            throw new RuntimeException("Access denied");
        }
        return card;
    }

    public RewardCardDTO getRewardCardByIdDTO(String cardId) {
        RewardCard card = getRewardCardById(cardId);
        return convertToDTO(card);
    }

    public List<RewardCardDTO> getAllRewardCards() {
        Long mallId = shoppingMallContext.getCurrentMallId();

        List<RewardCard> cards;

        if (mallId == null) {
            // ADMIN - sees everything
            cards = rewardCardRepo.findAll();
        } else {
            // MANAGER - sees only their mall
            cards = rewardCardRepo.findByShoppingMallId(mallId);
        }

        return cards.stream().map(this::convertToDTO).toList();
    }

    @Transactional
    public void activateCard(String cardId) {
        RewardCard card = getRewardCardById(cardId);
        card.setActive(true);
        rewardCardRepo.save(card);
    }

    @Transactional
    public void deactivateCard(String cardId) {
        RewardCard card = getRewardCardById(cardId);
        card.setActive(false);
        rewardCardRepo.save(card);
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
                    ? "0" + card.getCustomer().getPhone().toBigInteger() : "");
            dto.setCustomer(summary);
        }
        return dto;
    }
}
package org.sb_ibms.services;

import lombok.RequiredArgsConstructor;
import org.sb_ibms.dto.BillingRequest;
import org.sb_ibms.dto.BillingResponse;
import org.sb_ibms.dto.CartItemDetail;
import org.sb_ibms.dto.CartItemRequest;
import org.sb_ibms.enums.PaymentStatus;
import org.sb_ibms.models.RewardCard;
import org.sb_ibms.models.ShoppingMallCustomer;
import org.sb_ibms.models.ShoppingMallPayments;
import org.sb_ibms.models.ShoppingMallProduct;
import org.sb_ibms.repositories.ShoppingMallCustomerRepository;
import org.sb_ibms.repositories.ShoppingMallPaymentRepository;
import org.sb_ibms.repositories.ShoppingMallProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static reactor.netty.http.HttpConnectionLiveness.log;

@Service
@RequiredArgsConstructor
public class ShoppingMallPosBillingService {

    private final ShoppingMallProductRepository productRepo;
    private final ShoppingMallCustomerRepository customerRepo;
    private final ShoppingMallPaymentRepository paymentRepo;
    private final RewardService rewardService;

    public ShoppingMallProduct findByBarcode(String barcode) {
        return productRepo.findByBarcode(barcode)
                .orElseThrow(() -> new RuntimeException("Product not found with barcode: " + barcode));
    }

    public ShoppingMallProduct findById(Long id) {
        return productRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with barcode: " + id));
    }

    public ShoppingMallProduct findByName(String name) {
        return productRepo.findByName(name)
                .orElseThrow(() -> new RuntimeException("Product not found with barcode: " + name));
    }

    public ShoppingMallCustomer findCustomerByPhone(BigDecimal phone) {
        return customerRepo.findByPhone(phone);
    }

    @Transactional
    public BillingResponse createBill(BillingRequest req) {
        ShoppingMallCustomer customer = null;

        if (req.getCustomerPhone() != null) {
            customer = customerRepo.findByPhone(req.getCustomerPhone());
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        List<CartItemDetail> itemsSold = new ArrayList<>();

        for (CartItemRequest itemReq : req.getItems()) {
            ShoppingMallProduct product = findByBarcode(itemReq.getBarcode());

            if (product.getStock() < itemReq.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }

            BigDecimal itemTotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(itemReq.getQuantity()));

            subtotal = subtotal.add(itemTotal);

            product.setStock(product.getStock() - itemReq.getQuantity());
            productRepo.save(product);

            itemsSold.add(new CartItemDetail(product.getName(), itemReq.getQuantity(), itemTotal));
        }

        BigDecimal discount = req.getDiscountAmount() != null ? req.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal finalAmount = subtotal.subtract(discount);

        if (customer != null) {
            // Save Payment
            ShoppingMallPayments payment = new ShoppingMallPayments();
            payment.setShoppingMallCustomer(customer);
            payment.setPaidAmount(finalAmount);
            payment.setPaymentMethod(req.getPaymentMethod());
            payment.setTrxId(req.getTrxId());
            payment.setPaymentDate(LocalDate.now());
            payment.setStatus(PaymentStatus.SUCCEEDED);
            paymentRepo.save(payment);

            // Update Customer Totals
            if (customer.getPaidAmount() == null) customer.setPaidAmount(BigDecimal.ZERO);
            if (customer.getPurchaseAmount() == null) customer.setPurchaseAmount(BigDecimal.ZERO);

            customer.setPaidAmount(customer.getPaidAmount().add(finalAmount));
            customer.setDueAmount(customer.getPurchaseAmount().subtract(customer.getPaidAmount()));

            // === REWARD POINTS - FIXED ===
            if (finalAmount.compareTo(BigDecimal.valueOf(2000)) >= 0) {
                int points = finalAmount.intValue() / 2;   // Adjust logic as needed

                if (points > 0) {
                    try {
                        // Using your existing method
                        RewardCard rewardCard = rewardService.getRewardCardById(
                                String.valueOf(customer.getId())
                        );

                        if (rewardCard != null) {
                            rewardService.addPoints(rewardCard.getCardNumber(), points,
                                    "Purchased items worth " + finalAmount);
                        } else {
                            log.warn("No reward card found for customer ID: {}", customer.getId());
                        }
                    } catch (Exception e) {
                        log.error("Failed to add reward points for customer {}: {}", customer.getId(), e.getMessage());
                        // Important: Do not rethrow - let billing succeed
                    }
                }
            }

            customerRepo.save(customer);
        }

        return new BillingResponse(
                "BILL-" + System.currentTimeMillis(),
                finalAmount,
                customer != null,
                customer != null ? customer.getName() : null,
                "Sale completed successfully"
        );
    }
}
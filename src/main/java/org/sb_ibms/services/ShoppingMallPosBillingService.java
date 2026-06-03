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

@Service
@RequiredArgsConstructor
public class ShoppingMallPosBillingService {

    private final ShoppingMallProductRepository productRepo;
    private final ShoppingMallCustomerRepository customerRepo;
    private final ShoppingMallPaymentRepository paymentRepo;
    private final RewardService rewardService;
    private final ShoppingMallContext shoppingMallContext;
    private final ShoppingMallProductRepository shoppingMallProductRepository;
    private final ShoppingMallProductService productService;

    // Delegate to ProductService for better consistency
    public ShoppingMallProduct findByBarcode(String barcode) {
        return productService.getProductByBarcode(barcode);   // Reuse logic
    }

    public ShoppingMallProduct findById(Long id) {
        return productService.getProductById(id);
    }

    public ShoppingMallProduct findByName(String name) {
        return productService.getProductByName(name);
    }

    public ShoppingMallCustomer findCustomerByPhone(BigDecimal phone) {
        Long mallId = shoppingMallContext.getCurrentMallId();
        return customerRepo.findByPhoneAndShoppingMallId(phone, mallId);  // Return null if not found (common in POS)
    }

    @Transactional
    public BillingResponse createBill(BillingRequest req) {
        Long mallId = shoppingMallContext.getCurrentMallId();

        ShoppingMallCustomer customer = null;
        if (req.getCustomerPhone() != null) {
            customer = findCustomerByPhone(req.getCustomerPhone());
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

            // Update stock
            product.setStock(product.getStock() - itemReq.getQuantity());
            productRepo.save(product);

            itemsSold.add(new CartItemDetail(product.getName(), itemReq.getQuantity(), itemTotal));
        }

        BigDecimal discount = req.getDiscountAmount() != null ? req.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal finalAmount = subtotal.subtract(discount);

        if (customer != null) {
            // Create Payment Record
            ShoppingMallPayments payment = new ShoppingMallPayments();
            payment.setShoppingMallCustomer(customer);
            payment.setPaidAmount(finalAmount);
            payment.setPaymentMethod(req.getPaymentMethod());
            payment.setTrxId(req.getTrxId());
            payment.setPaymentDate(LocalDate.now());
            payment.setStatus(PaymentStatus.SUCCEEDED);
            payment.setShoppingMallId(mallId);
            paymentRepo.save(payment);

            // Update Customer
            customer.setPurchaseAmount((customer.getPurchaseAmount() != null ? customer.getPurchaseAmount() : BigDecimal.ZERO).add(finalAmount));
            customer.setPaidAmount((customer.getPaidAmount() != null ? customer.getPaidAmount() : BigDecimal.ZERO).add(finalAmount));
            customer.setDueAmount(customer.getPurchaseAmount().subtract(customer.getPaidAmount()));
            customer.setShoppingMallId(mallId);
            customerRepo.save(customer);

            // Reward Points
            if (finalAmount.compareTo(BigDecimal.valueOf(2000)) >= 0) {
                int points = finalAmount.intValue() / 2;
                if (points > 0) {
                    try {
                        RewardCard card = rewardService.getRewardCardByCustomer(String.valueOf(customer.getId()));
                        if (card != null) {
                            rewardService.addPoints(card.getCardNumber(), points, "Sale worth " + finalAmount);
                        }
                    } catch (Exception e) {
                        System.err.println("Reward points error: " + e.getMessage());
                    }
                }
            }
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
package org.sb_ibms.services;

import lombok.AllArgsConstructor;
import org.sb_ibms.dto.PaymentRequest;
import org.sb_ibms.dto.ShoppingMallPaymentRequest;
import org.sb_ibms.enums.OrderStatus;
import org.sb_ibms.enums.PaymentStatus;
import org.sb_ibms.models.*;
import org.sb_ibms.repositories.ShoppingMallCustomerRepository;
import org.sb_ibms.repositories.ShoppingMallPaymentRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@AllArgsConstructor
public class ShoppingMallPaymentServiceImpl implements ShoppingMallPaymentInterface{
    private final ShoppingMallPaymentRepository paymentRepository;
    private final ShoppingMallCustomerRepository masterDataRepository;



    @Override
    public ShoppingMallPayments addPayment(Long masterDataId, ShoppingMallPaymentRequest request) {

        ShoppingMallCustomer masterData = masterDataRepository.findById(masterDataId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        // === Create Payment Record ===
        ShoppingMallPayments payment = new ShoppingMallPayments();
        payment.setShoppingMallCustomer(masterData);
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setPaidAmount(request.getPaidAmount());
        payment.setPaymentDate(request.getPaymentDate() != null ? request.getPaymentDate() : LocalDate.now());
        payment.setTrxId(request.getTrxId());
        payment.setStatus(PaymentStatus.SUCCEEDED);

        // === Update Customer Financials (Safe Null Handling) ===
        if (masterData.getPaidAmount() == null) {
            masterData.setPaidAmount(BigDecimal.ZERO);
        }
        if (masterData.getPurchaseAmount() == null) {
            masterData.setPurchaseAmount(BigDecimal.ZERO);   // Important fix
        }

        // Add the new payment
        masterData.setPaidAmount(
                masterData.getPaidAmount().add(payment.getPaidAmount())
        );

        // Safe due amount calculation
        masterData.setDueAmount(
                masterData.getPurchaseAmount().subtract(masterData.getPaidAmount())
        );

        // Optional: Update status
        if (masterData.getDueAmount().compareTo(BigDecimal.ZERO) <= 0) {
            masterData.setStatus(OrderStatus.PAID);   // or whatever your enum/field is
        } else {
            masterData.setStatus(OrderStatus.PARTIALLY_PAID);
        }

        masterDataRepository.save(masterData);
        return paymentRepository.save(payment);
    }

    @Override
    public List<ShoppingMallPayments> getPayments(Long masterDataId) {

        return paymentRepository.findByShoppingMallCustomerId(masterDataId);
    }

    @Override
    public ShoppingMallPayments processPayment(Long masterId) {


        ShoppingMallCustomer masterData = masterDataRepository.findById(masterId)
                .orElseThrow(() ->
                        new RuntimeException("Master data not found")
                );

        if (masterData.isPaymentCompleted()) {
            throw new RuntimeException("Payment already completed");
        }

        ShoppingMallPayments payment = new ShoppingMallPayments();
        payment.setShoppingMallCustomer(masterData);
        payment.setPaidAmount(masterData.getPaidAmount());
        payment.setPaymentMethod(masterData.getPaymentMethod());
        payment.setPaymentDate(LocalDate.now());
        payment.setStatus(PaymentStatus.SUCCEEDED);

        ShoppingMallPayments savedPayment = paymentRepository.save(payment);

        masterData.setPaymentCompleted(true);
        masterData.setPaidAmount(masterData.getPaidAmount());
        masterData.setDueAmount(BigDecimal.ZERO);
        masterData.setDate(LocalDate.now());

        masterDataRepository.save(masterData);

        return savedPayment;
    }
}

package org.sb_ibms.services;

import org.sb_ibms.dto.PaymentRequest;
import org.sb_ibms.dto.ShoppingMallPaymentRequest;
import org.sb_ibms.models.ShoppingMallPayments;

import java.util.List;

public interface ShoppingMallPaymentInterface {
    ShoppingMallPayments addPayment(Long masterDataId, ShoppingMallPaymentRequest request);
    List<ShoppingMallPayments> getPayments(Long masterDataId);
    ShoppingMallPayments processPayment(Long masterId);
}

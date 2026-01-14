package org.example.sbgroup2.services;

import org.example.sbgroup2.dto.PaymentRequest;
import org.example.sbgroup2.models.Payment;

import java.util.List;


public interface PaymentService2 {


    Payment addPayment(Long masterDataId, PaymentRequest request);

    List<Payment> getPayments(Long masterDataId);
    Payment processPayment(Long masterId);


}

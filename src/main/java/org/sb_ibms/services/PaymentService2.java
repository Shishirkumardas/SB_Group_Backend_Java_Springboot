package org.sb_ibms.services;
import org.sb_ibms.dto.PaymentRequest;
import org.sb_ibms.models.Payment;
import java.util.List;


public interface PaymentService2 {


    Payment addPayment(Long masterDataId, PaymentRequest request);
    List<Payment> getPayments(Long masterDataId);
    Payment processPayment(Long masterId);


}

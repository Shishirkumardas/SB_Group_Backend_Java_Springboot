package org.example.sbgroup2.services;

import org.example.sbgroup2.ResourceNotFoundException;
import org.example.sbgroup2.models.Payment;
import org.example.sbgroup2.repositories.AreaRepository;
import org.example.sbgroup2.repositories.PaymentRepository;
import org.example.sbgroup2.repositories.PaymentRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentService {
        @Autowired
    private PaymentRepository paymentRepository;
    private AreaRepository areaRepository;
    private PaymentRequestRepository paymentRequestRepository;


    public Payment updatePayment(Long id, Payment paymentDetails) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        payment.setPaymentDate(paymentDetails.getPaymentDate());
        payment.setPaidAmount(paymentDetails.getPaidAmount());
        return paymentRepository.save(payment);
    }


    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
    }

    public Payment createPayment(Payment payment) {
        return paymentRepository.save(payment);
    }



    public void deletePayment(Long id) {
        Payment payment = getPaymentById(id);
        paymentRepository.delete(payment);
    }

}

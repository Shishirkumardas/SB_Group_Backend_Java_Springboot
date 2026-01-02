package org.example.sbgroup2.services;


import jakarta.transaction.Transactional;
import org.example.sbgroup2.dto.PaymentRequest;
import org.example.sbgroup2.enums.PaymentStatus;
import org.example.sbgroup2.models.MasterData;
import org.example.sbgroup2.models.Payment;
import org.example.sbgroup2.repositories.MasterDataRepository;
import org.example.sbgroup2.repositories.PaymentRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


@Service
@Transactional
public class PaymentServiceImpl implements PaymentService2 {

    private final PaymentRepository paymentRepository;
    private final MasterDataRepository masterDataRepository;

    public PaymentServiceImpl(
            PaymentRepository paymentRepository,
            MasterDataRepository masterDataRepository
    ) {
        this.paymentRepository = paymentRepository;
        this.masterDataRepository = masterDataRepository;
    }

    @Override
    public Payment addPayment(Long masterDataId, PaymentRequest request) {

        MasterData masterData = masterDataRepository.findById(masterDataId)
                .orElseThrow(() -> new RuntimeException("Master data not found"));

        Payment payment = new Payment();
        payment.setMasterData(masterData);
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setPaidAmount(request.getAmount());
        payment.setPaymentDate(request.getPaymentDate());

        // 🔁 Update paid & due amount automatically
        if (masterData.getPaidAmount() == null) {
            masterData.setPaidAmount(BigDecimal.ZERO);
        }

        masterData.setPaidAmount(
                masterData.getPaidAmount().add(request.getAmount())
        );

        masterData.setDueAmount(
                masterData.getPurchaseAmount()
                        .subtract(masterData.getPaidAmount())
        );

        masterDataRepository.save(masterData);

        return paymentRepository.save(payment);
    }

    @Override
    public List<Payment> getPayments(Long masterDataId) {

        return paymentRepository.findByMasterDataId(masterDataId);
    }

    @Override
    public Payment processPayment(Long masterId) {

        // 1️⃣ Load MasterData
        MasterData masterData = masterDataRepository.findById(masterId)
                .orElseThrow(() ->
                        new RuntimeException("Master data not found")
                );

        // 2️⃣ Prevent duplicate payment
        if (masterData.isPaymentCompleted()) {
            throw new RuntimeException("Payment already completed");
        }

        // 3️⃣ Create payment record
        Payment payment = new Payment();
        payment.setMasterData(masterData);
        payment.setPaidAmount(masterData.getPaidAmount());
        payment.setPaymentMethod(masterData.getPaymentMethod());
        payment.setPaymentDate(LocalDate.now());
        payment.setStatus(PaymentStatus.SUCCEEDED);

        Payment savedPayment = paymentRepository.save(payment);

        // 4️⃣ Update master data status
        masterData.setPaymentCompleted(true);
        masterData.setPaidAmount(masterData.getPaidAmount());
        masterData.setDueAmount(BigDecimal.ZERO);
        masterData.setDate(LocalDate.now());

        masterDataRepository.save(masterData);

        return savedPayment;
    }
}


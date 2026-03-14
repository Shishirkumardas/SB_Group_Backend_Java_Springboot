package org.sb_ibms.services;


import jakarta.transaction.Transactional;
import org.sb_ibms.dto.PaymentRequest;
import org.sb_ibms.enums.PaymentStatus;
import org.sb_ibms.models.MasterData;
import org.sb_ibms.models.Payment;
import org.sb_ibms.repositories.MasterDataRepository;
import org.sb_ibms.repositories.PaymentRepository;
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


        MasterData masterData = masterDataRepository.findById(masterId)
                .orElseThrow(() ->
                        new RuntimeException("Master data not found")
                );

        if (masterData.isPaymentCompleted()) {
            throw new RuntimeException("Payment already completed");
        }

        Payment payment = new Payment();
        payment.setMasterData(masterData);
        payment.setPaidAmount(masterData.getPaidAmount());
        payment.setPaymentMethod(masterData.getPaymentMethod());
        payment.setPaymentDate(LocalDate.now());
        payment.setStatus(PaymentStatus.SUCCEEDED);

        Payment savedPayment = paymentRepository.save(payment);

        masterData.setPaymentCompleted(true);
        masterData.setPaidAmount(masterData.getPaidAmount());
        masterData.setDueAmount(BigDecimal.ZERO);
        masterData.setDate(LocalDate.now());

        masterDataRepository.save(masterData);

        return savedPayment;
    }
}


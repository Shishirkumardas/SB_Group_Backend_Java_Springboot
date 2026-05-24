package org.sb_ibms.services;

import lombok.AllArgsConstructor;
import org.sb_ibms.ResourceNotFoundException;
import org.sb_ibms.enums.OrderStatus;
import org.sb_ibms.enums.PaymentMethod;
import org.sb_ibms.enums.PaymentStatus;
import org.sb_ibms.enums.ShoppingMallPaymentMethod;
import org.sb_ibms.models.Payment;
import org.sb_ibms.models.ShoppingMallCustomer;
import org.sb_ibms.models.ShoppingMallPayments;
import org.sb_ibms.repositories.ShoppingMallPaymentRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class ShoppingMallPaymentService {
    private ShoppingMallPaymentRepository paymentRepository;
    private AreaService areaService;
    private ShoppingMallMasterDataService masterDataService;


    public ShoppingMallPayments updatePayment(Long id, Payment paymentDetails) {
        ShoppingMallPayments payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        payment.setPaymentDate(paymentDetails.getPaymentDate());
        payment.setPaidAmount(paymentDetails.getPaidAmount());
        return paymentRepository.save(payment);
    }


    public ShoppingMallPayments getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
    }

    public ShoppingMallPayments createPayment(ShoppingMallPayments payment) {
        return paymentRepository.save(payment);
    }

    public void createPendingPayment(ShoppingMallCustomer masterData, BigDecimal amount, String trxId) {

        ShoppingMallPayments payment = ShoppingMallPayments.builder()
                .shoppingMallCustomer(masterData)
                .paidAmount(amount)
                .paymentMethod(ShoppingMallPaymentMethod.BKASH)
                .paymentDate(LocalDate.from(LocalDateTime.now()))
                .trxId(trxId)
                .status(PaymentStatus.PAYMENT_CREATED)
                .build();

        paymentRepository.save(payment);
    }

    public void markPaymentSuccess(String trxId) {
        ShoppingMallPayments payment = paymentRepository.findByTrxId(trxId);


        payment.setStatus(PaymentStatus.SUCCEEDED);
        paymentRepository.save(payment);

        ShoppingMallCustomer md = payment.getShoppingMallCustomer();

        BigDecimal newPaid = md.getPaidAmount().add(payment.getPaidAmount());
        md.setPaidAmount(newPaid);
        md.setDueAmount(md.getPurchaseAmount().subtract(newPaid));

        if (md.getDueAmount().compareTo(BigDecimal.ZERO) <= 0) {
            md.setStatus(OrderStatus.PAID);
        } else {
            md.setStatus(OrderStatus.PARTIALLY_PAID);
        }

        masterDataService.updateMasterData(md.getId(), md);

        areaService.recalculateArea(md.getArea().getId());
    }

    public void markPaymentFailed(String trxId) {
        ShoppingMallPayments payment = paymentRepository.findByTrxId(trxId);

        payment.setStatus(PaymentStatus.FAILED);
        paymentRepository.save(payment);
    }


    public void deletePayment(Long id) {
        ShoppingMallPayments payment = getPaymentById(id);
        paymentRepository.delete(payment);
    }
}

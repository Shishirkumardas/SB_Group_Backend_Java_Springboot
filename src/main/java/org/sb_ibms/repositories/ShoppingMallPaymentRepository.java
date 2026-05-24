package org.sb_ibms.repositories;

import org.sb_ibms.models.ShoppingMallPayments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface ShoppingMallPaymentRepository extends JpaRepository<ShoppingMallPayments, Long> {

    List<ShoppingMallPayments> findByShoppingMallCustomerId(Long customerId);
    ShoppingMallPayments findByTrxId(String trxId);
}

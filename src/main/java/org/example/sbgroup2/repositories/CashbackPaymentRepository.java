package org.example.sbgroup2.repositories;

import org.example.sbgroup2.models.CashbackPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CashbackPaymentRepository
        extends JpaRepository<CashbackPayment, Long> {

    List<CashbackPayment> findByMasterDataId(Long masterDataId);
}
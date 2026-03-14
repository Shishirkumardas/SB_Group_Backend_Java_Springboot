package org.sb_ibms.repositories;

import org.sb_ibms.models.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByMasterDataId(Long masterDataId);
    Payment findByTrxId(String trxId);
}

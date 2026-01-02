package org.example.sbgroup2.repositories;

import org.example.sbgroup2.models.Payment;
import org.example.sbgroup2.services.AreaSummaryDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {


    List<Payment> findByMasterDataId(Long masterDataId);
}

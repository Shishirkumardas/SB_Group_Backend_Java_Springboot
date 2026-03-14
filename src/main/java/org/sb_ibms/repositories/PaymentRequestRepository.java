package org.sb_ibms.repositories;

import org.sb_ibms.dto.PaymentRequest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRequestRepository extends JpaRepository<PaymentRequest, Long> {

}

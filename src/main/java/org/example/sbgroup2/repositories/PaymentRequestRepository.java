package org.example.sbgroup2.repositories;

import org.example.sbgroup2.dto.PaymentRequest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRequestRepository extends JpaRepository<PaymentRequest, Long> {



}

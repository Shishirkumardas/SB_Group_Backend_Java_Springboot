package org.sb_ibms.repositories;

import org.sb_ibms.models.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {}

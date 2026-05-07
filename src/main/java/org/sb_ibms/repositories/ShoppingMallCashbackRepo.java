package org.sb_ibms.repositories;

import org.sb_ibms.models.ShoppingMallCashback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShoppingMallCashbackRepo extends JpaRepository<ShoppingMallCashback, Long> {
}

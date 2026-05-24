package org.sb_ibms.repositories;

import org.sb_ibms.models.ShoppingMallSale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShoppingMallSaleRepository extends JpaRepository<ShoppingMallSale, String> {
}

package org.sb_ibms.repositories;


import org.sb_ibms.models.ShoppingMallSaleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShoppingMallSaleItemRepository extends JpaRepository<ShoppingMallSaleItem, String> {
}

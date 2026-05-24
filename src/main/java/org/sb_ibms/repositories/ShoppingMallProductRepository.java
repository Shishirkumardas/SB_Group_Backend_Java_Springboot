package org.sb_ibms.repositories;

import org.sb_ibms.models.ShoppingMallProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShoppingMallProductRepository extends JpaRepository<ShoppingMallProduct, Long> {
    Optional<ShoppingMallProduct> findByBarcode(String barcode);

    Optional<ShoppingMallProduct> findByName(String name);
}

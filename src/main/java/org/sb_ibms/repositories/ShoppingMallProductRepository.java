package org.sb_ibms.repositories;

import org.sb_ibms.models.ShoppingMallProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShoppingMallProductRepository extends JpaRepository<ShoppingMallProduct, Long> {
    Optional<ShoppingMallProduct> findByBarcode(String barcode);

    Optional<ShoppingMallProduct> findByName(String name);

    List<ShoppingMallProduct> findByShoppingMallId(Long shoppingMallId);

    Optional<ShoppingMallProduct> findByBarcodeAndShoppingMallId(String barcode, Long shoppingMallId);

    Optional<ShoppingMallProduct> findByNameAndShoppingMallId(String name, Long shoppingMallId);

// ==================== SEARCH METHODS ====================

    /**
     * Search by name or barcode (Admin - sees all malls)
     */
    @Query("""
        SELECT p FROM ShoppingMallProduct p 
        WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) 
           OR LOWER(p.barcode) LIKE LOWER(CONCAT('%', :query, '%'))
        ORDER BY p.name ASC
    """)
    List<ShoppingMallProduct> searchByNameOrBarcode(@Param("query") String query);

    /**
     * Search by name or barcode within a specific mall (Manager)
     */
    @Query("""
        SELECT p FROM ShoppingMallProduct p 
        WHERE p.shoppingMallId = :mallId
          AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) 
           OR LOWER(p.barcode) LIKE LOWER(CONCAT('%', :query, '%')))
        ORDER BY p.name ASC
    """)
    List<ShoppingMallProduct> searchByNameOrBarcodeInMall(
            @Param("query") String query,
            @Param("mallId") Long mallId
    );

    // Optional: Limit results
    @Query("""
        SELECT p FROM ShoppingMallProduct p 
        WHERE p.shoppingMallId = :mallId
          AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) 
           OR p.barcode LIKE CONCAT('%', :query, '%'))
        ORDER BY p.name ASC
    """)
    List<ShoppingMallProduct> searchByNameOrBarcodeInMallLimit(
            @Param("query") String query,
            @Param("mallId") Long mallId
    );
}
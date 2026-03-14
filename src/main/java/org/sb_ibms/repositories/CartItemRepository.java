package org.sb_ibms.repositories;

import org.sb_ibms.models.CartItem;
import org.sb_ibms.models.Product;
import org.sb_ibms.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, String> {
    List<CartItem> findByUserId(String userId);

    Optional<CartItem> findByUserAndProduct(User user, Product product);
}
package org.example.sbgroup2.repositories;



import org.example.sbgroup2.models.CartItem;
import org.example.sbgroup2.models.Product;
import org.example.sbgroup2.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, String> {
    List<CartItem> findByUserId(String userId);

    Optional<CartItem> findByUserAndProduct(User user, Product product);
}
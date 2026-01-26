package org.example.sbgroup2.repositories;


import org.example.sbgroup2.models.Order;
import org.example.sbgroup2.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);

    List<Order> findByUserId(String userId);
}

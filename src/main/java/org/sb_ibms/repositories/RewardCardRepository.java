package org.sb_ibms.repositories;

import org.sb_ibms.models.Product;
import org.sb_ibms.models.RewardCard;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RewardCardRepository extends JpaRepository<RewardCard, Long> {
    boolean existsByCustomerId(Long customer_id);

    Optional<RewardCard> findByCustomerId(Long customer_id);

    // Important: Fetch customer data eagerly
    @EntityGraph(attributePaths = {"customer"})
    List<RewardCard> findAll();

}

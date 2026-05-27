package org.sb_ibms.repositories;

import org.sb_ibms.models.ShoppingMallArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShoppingMallAreaRepository extends JpaRepository<ShoppingMallArea, Long> {

    ShoppingMallArea findByName(String name);
}

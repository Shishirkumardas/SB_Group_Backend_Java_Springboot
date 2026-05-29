package org.sb_ibms.repositories;

import org.sb_ibms.models.UserShoppingMallMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface UserShoppingMallMappingRepository extends JpaRepository<UserShoppingMallMapping, Long> {

    // Find all malls assigned to a user
    List<UserShoppingMallMapping> findByUserId(String userId);

    // Find all users assigned to a mall
    List<UserShoppingMallMapping> findByShoppingMallId(Long shoppingMallId);

    // Check if a specific user is assigned to a mall
    boolean existsByUserIdAndShoppingMallId(String userId, Long shoppingMallId);

    // Delete all assignments for a user
    @Modifying
    @Transactional
    @Query("DELETE FROM UserShoppingMallMapping m WHERE m.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    // Delete specific assignment
    @Modifying
    @Transactional
    void deleteByUserIdAndShoppingMallId(String userId, Long shoppingMallId);
}

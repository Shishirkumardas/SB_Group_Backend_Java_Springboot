package org.sb_ibms.repositories;

import org.sb_ibms.dto.MallListDTO;
import org.sb_ibms.models.ShoppingMall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ShoppingMallRepository extends JpaRepository<ShoppingMall, Long> {

    // For Admin
    @Query("""
        SELECT new org.sb_ibms.dto.MallListDTO(
            s.id, s.name, s.areaName, s.address
        )
        FROM ShoppingMall s
        ORDER BY s.name
        """)
    List<MallListDTO> findAllMallsAsDTO();

    // For Manager (via mapping table)
    @Query("""
        SELECT new org.sb_ibms.dto.MallListDTO(
            s.id, s.name, s.areaName, s.address
        )
        FROM ShoppingMall s
        JOIN UserShoppingMallMapping m ON s.id = m.shoppingMallId
        WHERE m.userId = :userId
        ORDER BY s.name
        """)
    List<MallListDTO> findMallsByManagerId(@Param("userId") String userId);
}

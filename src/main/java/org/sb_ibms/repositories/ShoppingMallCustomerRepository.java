package org.sb_ibms.repositories;

import org.sb_ibms.dto.OverallSummary;
import org.sb_ibms.dto.PaymentView;
import org.sb_ibms.dto.PurchaseView;
import org.sb_ibms.dto.ShoppingMallPaymentView;
import org.sb_ibms.models.ShoppingMallCustomer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ShoppingMallCustomerRepository extends JpaRepository<ShoppingMallCustomer, Long> {

    ShoppingMallCustomer findByName(String name);

    @Query("""
        SELECT COALESCE(SUM(m.purchaseAmount), 0)
        FROM ShoppingMallCustomer m
        WHERE m.area.id = :areaId
    """)
    BigDecimal sumPurchaseByArea(Long areaId);

    @Query("""
        SELECT COALESCE(SUM(m.quantity), 0)
        FROM ShoppingMallCustomer m
        WHERE m.area.id = :areaId
    """)
    BigDecimal sumQuantityByArea(Long areaId);

    @Query("""
        SELECT COALESCE(SUM(m.paidAmount), 0)
        FROM ShoppingMallCustomer m
        WHERE m.area.id = :areaId
    """)
    BigDecimal sumPaidByArea(Long areaId);


    @Query("""
    SELECT COALESCE(SUM(m.purchaseAmount), 0) FROM ShoppingMallCustomer m WHERE m.area.id = :areaId AND m.date = :date
""")
    BigDecimal sumPurchaseByAreaAndDate(
            @Param("areaId") Long areaId,
            @Param("date") LocalDate date
    );

    @Query("""
    SELECT COALESCE(SUM(m.quantity), 0) FROM ShoppingMallCustomer m WHERE m.area.id = :areaId AND m.date = :date
""")
    BigDecimal sumQuantityByAreaAndDate(
            @Param("areaId") Long areaId,
            @Param("date") LocalDate date
    );

    @Query("""
    SELECT new org.sb_ibms.dto.ShoppingMallPaymentView(
        p.paymentDate,
        p.paidAmount,
        p.paymentMethod,
        c.name,
        c.phone
    )
    FROM ShoppingMallPayments p
    JOIN p.shoppingMallCustomer c
    WHERE p.paidAmount > 0
    ORDER BY p.paymentDate DESC
""")
    List<ShoppingMallPaymentView> getPayments();

    @Query("""
        SELECT new org.sb_ibms.dto.PurchaseView(
            m.date,
            m.purchaseAmount
        )
        FROM ShoppingMallCustomer m
        WHERE m.purchaseAmount > 0
        ORDER BY m.date DESC
    """)
    List<PurchaseView> getPurchases();

    @Query("""
    SELECT new org.sb_ibms.dto.OverallSummary(
        SUM(m.purchaseAmount),
        SUM(m.paidAmount),
        SUM(m.dueAmount)
    )
    FROM ShoppingMallCustomer m
""")
    OverallSummary getSummary();

    ShoppingMallCustomer findByPhone(BigDecimal normalizedPhone);
}

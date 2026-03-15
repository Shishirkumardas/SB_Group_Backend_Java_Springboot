package org.sb_ibms.repositories;
import org.sb_ibms.dto.OverallSummary;
import org.sb_ibms.dto.PaymentView;
import org.sb_ibms.dto.PurchaseView;
import org.sb_ibms.models.MasterData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface MasterDataRepository extends JpaRepository<MasterData, Long> {
    MasterData findByName(String name);

    @Query("""
        SELECT COALESCE(SUM(m.purchaseAmount), 0)
        FROM MasterData m
        WHERE m.area.id = :areaId
    """)
    BigDecimal sumPurchaseByArea(Long areaId);

    @Query("""
        SELECT COALESCE(SUM(m.quantity), 0)
        FROM MasterData m
        WHERE m.area.id = :areaId
    """)
    BigDecimal sumQuantityByArea(Long areaId);

    @Query("""
        SELECT COALESCE(SUM(m.paidAmount), 0)
        FROM MasterData m
        WHERE m.area.id = :areaId
    """)
    BigDecimal sumPaidByArea(Long areaId);


    @Query("""
    SELECT COALESCE(SUM(m.purchaseAmount), 0) FROM MasterData m WHERE m.area.id = :areaId AND m.date = :date
""")
    BigDecimal sumPurchaseByAreaAndDate(
            @Param("areaId") Long areaId,
            @Param("date") LocalDate date
    );

    @Query("""
    SELECT COALESCE(SUM(m.quantity), 0) FROM MasterData m WHERE m.area.id = :areaId AND m.date = :date
""")
    BigDecimal sumQuantityByAreaAndDate(
            @Param("areaId") Long areaId,
            @Param("date") LocalDate date
    );

    @Query("""
        SELECT new org.sb_ibms.dto.PaymentView(
            m.date,
            m.paidAmount
        )
        FROM MasterData m
        WHERE m.paidAmount > 0
        ORDER BY m.date DESC
    """)
    List<PaymentView> getPayments();

    @Query("""
        SELECT new org.sb_ibms.dto.PurchaseView(
            m.date,
            m.purchaseAmount
        )
        FROM MasterData m
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
    FROM MasterData m
""")
    OverallSummary getSummary();

    List<MasterData> findByPhone(BigDecimal normalizedPhone);
}

package org.example.sbgroup2.repositories;


import org.example.sbgroup2.dto.ConsumerView;
import org.example.sbgroup2.dto.OverallSummary;
import org.example.sbgroup2.dto.PaymentView;
import org.example.sbgroup2.dto.PurchaseView;
import org.example.sbgroup2.models.MasterData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface MasterDataRepository extends JpaRepository<MasterData, Long> {
    MasterData findByName(String name);

    List<MasterData> findByNextDueDate(LocalDate nextDueDate);

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
        SELECT COALESCE(SUM(m.amountBackFromPurchase), 0)
        FROM MasterData m
        WHERE m.area.id = :areaId
    """)
    BigDecimal sumCashbackByArea(Long areaId);

    @Query("""
        SELECT COALESCE(SUM(m.paidAmount), 0)
        FROM MasterData m
        WHERE m.area.id = :areaId
    """)
    BigDecimal sumPaidByArea(Long areaId);

    @Query("SELECT m FROM MasterData m " +
            "WHERE m.nextDueDate IS NOT NULL " +
            "AND m.nextDueDate = :maxDueDate " +
            "ORDER BY m.nextDueDate ASC")
    List<MasterData> findByNextDueDateLessThanEqual(@Param("maxDueDate") LocalDate maxDueDate);


    @Query("""
    SELECT COALESCE(SUM(m.purchaseAmount), 0) 
    FROM MasterData m 
    WHERE m.area.id = :areaId 
    AND m.date = :date
""")
    BigDecimal sumPurchaseByAreaAndDate(
            @Param("areaId") Long areaId,
            @Param("date") LocalDate date
    );

    @Query("""
    SELECT COALESCE(SUM(m.quantity), 0) 
    FROM MasterData m 
    WHERE m.area.id = :areaId 
    AND m.date = :date
""")
    BigDecimal sumQuantityByAreaAndDate(
            @Param("areaId") Long areaId,
            @Param("date") LocalDate date
    );

    @Query("""
    SELECT COALESCE(SUM(m.amountBackFromPurchase), 0) 
    FROM MasterData m 
    WHERE m.area.id = :areaId 
    AND m.date = :date
""")
    BigDecimal sumCashbackByAreaAndDate(
            @Param("areaId") Long areaId,
            @Param("date") LocalDate date
    );





    /* ===============================
   CONSUMER VIEW (AUTO-SUMMED)
   =============================== */
    @Query("""
        SELECT new org.example.sbgroup2.dto.ConsumerView(
            m.name,
            m.area.name,
            SUM(m.purchaseAmount),
            SUM(m.paidAmount),
            SUM(m.dueAmount)
        )
        FROM MasterData m
        GROUP BY m.name
    """)
    List<ConsumerView> getConsumers();

    /* ===============================
       PAYMENT LEDGER
       =============================== */
    @Query("""
        SELECT new org.example.sbgroup2.dto.PaymentView(
            m.date,
            m.paidAmount
        )
        FROM MasterData m
        WHERE m.paidAmount > 0
        ORDER BY m.date DESC
    """)
    List<PaymentView> getPayments();


    /* ===============================
       PURCHASE LEDGER
       =============================== */
    @Query("""
        SELECT new org.example.sbgroup2.dto.PurchaseView(
            m.date,
            m.purchaseAmount
        )
        FROM MasterData m
        WHERE m.purchaseAmount > 0
        ORDER BY m.date DESC
    """)
    List<PurchaseView> getPurchases();


    /* ===============================
       OVERALL SUMMARY (DASHBOARD)
       =============================== */
//    @Query("""
//    SELECT new org.example.sbgroup2.dto.OverallSummary(
//        COALESCE(SUM(m.purchaseAmount), 0),
//        COALESCE(SUM(m.paidAmount), 0),
//        COALESCE(SUM(m.dueAmount), 0)
//    )
//    FROM MasterData m
//""")
//    OverallSummary getSummary();

    @Query("""
    SELECT new org.example.sbgroup2.dto.OverallSummary(
        SUM(m.purchaseAmount),
        SUM(m.paidAmount),
        SUM(m.dueAmount)
    )
    FROM MasterData m
""")
    OverallSummary getSummary();

//    @Query("""
//SELECT new org.example.sbgroup2.dto.OverallSummary(
//    SUM(m.purchaseAmount * 0.10),
//    SUM(cp.amount),
//    SUM((m.purchaseAmount * 0.10) - cp.amount)
//)
//FROM MasterData m
//LEFT JOIN CashbackPayment cp ON cp.masterData = m
//WHERE m.area.id = :areaId
//""")
//    OverallSummary areaCashbackSummary(Long areaId);

}

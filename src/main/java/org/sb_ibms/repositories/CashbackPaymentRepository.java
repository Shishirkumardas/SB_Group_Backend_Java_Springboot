package org.sb_ibms.repositories;

import org.sb_ibms.models.CashbackPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface CashbackPaymentRepository
        extends JpaRepository<CashbackPayment, Long> {

    List<CashbackPayment> findByMasterDataId(Long masterDataId);

    @Query("SELECT SUM(c.amount) FROM CashbackPayment c " +
            "WHERE c.masterData.area.id = :areaId AND c.paymentDate = :date")
    BigDecimal sumCashbackByAreaAndDate(@Param("areaId") Long areaId, @Param("date") LocalDate date);

    @Query("SELECT COUNT(c) FROM CashbackPayment c " +
            "WHERE c.masterData.area.id = :areaId AND c.paymentDate = :date")
    Long countCashbackByAreaAndDate(@Param("areaId") Long areaId, @Param("date") LocalDate date);

    @Query("""
    SELECT COALESCE(SUM(c.amount), 0) FROM CashbackPayment c WHERE c.masterData.area.id = :areaId
""")
    BigDecimal sumCashbackByArea(@Param("areaId") Long areaId);

}
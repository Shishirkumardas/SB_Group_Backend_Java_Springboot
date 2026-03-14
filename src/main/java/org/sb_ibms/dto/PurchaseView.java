package org.sb_ibms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class PurchaseView {
    private LocalDate date;
    private BigDecimal purchaseAmount;
}


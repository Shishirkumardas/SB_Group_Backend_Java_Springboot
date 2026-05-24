package org.sb_ibms.dto;

import lombok.Data;

import java.math.BigDecimal;
@Data
public class ShoppingMallCustomerFormDTO {
    private String customerName;
    private BigDecimal nid;
    private Long areaID;
    private BigDecimal phoneNumber;
}

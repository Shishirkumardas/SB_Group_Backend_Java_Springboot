package org.sb_ibms.dto;

import lombok.Getter;
import lombok.Setter;
import org.sb_ibms.enums.Role;


import java.math.BigDecimal;
import java.time.LocalDateTime;
@Getter
@Setter
public class userDTO {
    private String id;
    private String name;
    private String email;
    private String phone;
    private String address;
    private Role role;

    private String managerId;
    private String managerName;
    private String managerRole;
    private Long shoppingMallId;
    private String employeeCode;
    private Integer subordinatesCount = 0;

    // Performance Fields
    private BigDecimal netSale = BigDecimal.ZERO;
    private BigDecimal profit = BigDecimal.ZERO;
    private BigDecimal commission = BigDecimal.ZERO;

    private String password;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean active;
}

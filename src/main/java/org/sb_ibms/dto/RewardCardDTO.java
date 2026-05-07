package org.sb_ibms.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RewardCardDTO {
    private Long id;
    private String cardNumber;
    private int totalPoints;
    private LocalDateTime issuedAt;
    private boolean isActive;

    private CustomerSummary customer;

    public void setIsActive(boolean active) {
    }

    @Data
    public static class CustomerSummary {
        private Long id;
        private String name;
        private String phone;
    }
}
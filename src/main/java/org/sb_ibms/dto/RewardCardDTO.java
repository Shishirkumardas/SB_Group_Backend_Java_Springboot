package org.sb_ibms.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Data

public class RewardCardDTO {

    private Long id;
    private String cardNumber;
    private int totalPoints;
    private LocalDateTime issuedAt;

    @JsonProperty("isActive")
    private boolean isActive;

    private CustomerSummary customer;

    @Data
    public static class CustomerSummary {
        private Long id;
        private String name;
        private String phone;
    }
}
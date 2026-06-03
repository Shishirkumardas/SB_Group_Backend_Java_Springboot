package org.sb_ibms.dto;

import lombok.Data;

@Data
public class RedeemRequest {
    private String cardId;
    private int pointsToRedeem;
    private String remarks;

}

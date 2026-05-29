package org.sb_ibms.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ShoppingMallRequest {

    private String name;
    private String areaName;
    private String address;
    private String phone;
    private String email;
    private String location;
    private Integer totalShops;
    private LocalDateTime openingDate;
    private Boolean isActive;
    private Long ownerId;
}
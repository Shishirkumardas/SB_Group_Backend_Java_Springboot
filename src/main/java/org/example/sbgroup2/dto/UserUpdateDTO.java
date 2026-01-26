package org.example.sbgroup2.dto;

import lombok.Data;

@Data
public class UserUpdateDTO {
    private Long id;
    private String fullName;
    private String phone;
    private String address;
    // no password, no username, no email (or add email with validation)
}

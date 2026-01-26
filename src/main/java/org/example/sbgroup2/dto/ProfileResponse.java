package org.example.sbgroup2.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class ProfileResponse {
    private String id;
    private String name;
    private String email;
    private String role;
    private String phone;
    private String address;
}

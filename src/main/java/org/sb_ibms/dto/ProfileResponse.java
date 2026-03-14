package org.sb_ibms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

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

package org.sb_ibms.dto;

import lombok.Getter;
import lombok.Setter;
import org.sb_ibms.enums.Role;


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

    private String password;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean active;
}

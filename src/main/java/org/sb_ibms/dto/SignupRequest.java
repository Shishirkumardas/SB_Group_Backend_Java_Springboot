package org.sb_ibms.dto;

import lombok.Getter;
import lombok.Setter;
import org.sb_ibms.enums.Role;

@Getter
@Setter
public class SignupRequest {
    private String email;
    private String password;
    private Role role;
}


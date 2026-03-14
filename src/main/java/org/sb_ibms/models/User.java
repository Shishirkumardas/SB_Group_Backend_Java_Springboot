package org.sb_ibms.models;

import jakarta.persistence.*;
import lombok.Data;
import org.sb_ibms.enums.Role;


import java.util.UUID;


@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    private String id = UUID.randomUUID().toString();

    private String name;

    @Column(unique = true, nullable = false)
    private String email;
    private String phoneNumber;
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
    private String address;
    public User() {}

    public User(String email, String password, Role role) {
        this.email = email;
        this.password = password;
        this.role = role;
    }
}


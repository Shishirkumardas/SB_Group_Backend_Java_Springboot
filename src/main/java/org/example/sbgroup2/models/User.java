package org.example.sbgroup2.models;

import jakarta.persistence.*;
import lombok.Data;
import org.example.sbgroup2.enums.Role;
import org.jspecify.annotations.Nullable;

import java.util.UUID;


@Entity
@Table(name = "users")
@Data
public class User {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private String id = UUID.randomUUID().toString();

    @Column(unique = true, nullable = false)
    private String email;
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
// ADMIN or CUSTOMER
//    private Role role;


//    public User(String email, @Nullable String encode, String roleUser) {
//    }

//    public User() {}
//
//    public User(String email, String password, String role) {
//        this.email = email;
//        this.password = password;
//        this.role = role;
//    }

    public User() {}

    public User(String email, String password, Role role) {
        this.email = email;
        this.password = password;
        this.role = role;
    }
}


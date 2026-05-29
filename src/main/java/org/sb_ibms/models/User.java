package org.sb_ibms.models;

import jakarta.persistence.*;
import lombok.Data;
import org.sb_ibms.enums.Role;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
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



    // Hierarchy (only for internal roles like GM, AGM, ME, etc.)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private User manager;                    // GM has null, AGM points to GM, ME to AGM, etc.

    @OneToMany(mappedBy = "manager", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<User> subordinates = new ArrayList<>();


    @Column(name = "shopping_mall_id")
    private Long shoppingMallId;

    // nullable – only for mall roles
    private String employeeCode;

    // Performance Fields
    private BigDecimal netSale;
    private BigDecimal profit;
    private BigDecimal commission;

    public User() {}

    public User(String email, String password, Role role) {
        this.email = email;
        this.password = password;
        this.role = role;
    }
}


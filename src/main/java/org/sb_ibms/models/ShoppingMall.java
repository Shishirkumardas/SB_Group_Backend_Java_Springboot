package org.sb_ibms.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "shopping_malls")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingMall {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "area_name", length = 100)
    private String areaName;

    @Column(length = 255)
    private String address;

    @Column(length = 20)
    private String phone;

    @Column(length = 100)
    private String email;

    private String location;           // e.g., GPS coordinates or landmark

    @Column(name = "total_shops")
    private Integer totalShops;

    @Column(name = "opening_date")
    private LocalDateTime openingDate;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Optional: Link to owner/admin
    @Column(name = "owner_id")
    private Long ownerId;

    // === Lifecycle Hooks ===
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.isActive == null) this.isActive = true;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

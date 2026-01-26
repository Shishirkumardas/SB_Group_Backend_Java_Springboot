package org.example.sbgroup2.models;

import jakarta.persistence.*;
import lombok.*;


import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private double price;
    private int stock;
    private String category;
    private String subCategory;
//    @Column(nullable = true)
//    private double discount;

//    @Enumerated(EnumType.STRING)
//    private Brand brand; // RECKS or SHREYOSHI_SHAREE

    private String imageUrl;
    @ElementCollection
    private List<String> extraImages = new ArrayList<>();

//    @Column(nullable = false)
//    private boolean deleted = false;
}

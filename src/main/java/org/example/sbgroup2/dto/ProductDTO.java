package org.example.sbgroup2.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    private double price;
    private int stock;
    private String category;
    private String subCategory;
    private String imageUrl;
    private String brand;
    private double discount;
    private String createdAt;
}

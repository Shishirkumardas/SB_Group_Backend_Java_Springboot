package org.example.sbgroup2.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {
    private String name;
    private String description;
    private double price;
    private int stock;
    private String category;       // main category
    private String subCategory;    // sub category

    //    private MultipartFile image;   // ← file upload
    private List<MultipartFile> images = new ArrayList<>();
}

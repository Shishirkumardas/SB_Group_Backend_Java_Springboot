package org.example.sbgroup2.dto;


import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Data
public class ProductCreateDTO {
    private String name;
    private String description;
    private double price;
    private int stock;
    private String category;       // main category
    private String subCategory;    // sub category

//    private MultipartFile image;   // ← file upload
    private List<MultipartFile> images = new ArrayList<>();
}

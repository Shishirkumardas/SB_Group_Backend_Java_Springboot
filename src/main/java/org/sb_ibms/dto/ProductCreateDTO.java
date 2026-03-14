package org.sb_ibms.dto;


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
    private String category;
    private String subCategory;
    private List<MultipartFile> images = new ArrayList<>();
}

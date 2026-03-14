package org.sb_ibms.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.sb_ibms.dto.ProductCreateDTO;
import org.sb_ibms.dto.ProductDTO;
import org.sb_ibms.models.Product;
import org.sb_ibms.repositories.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.ArrayList;
import java.util.List;

import static reactor.netty.http.HttpConnectionLiveness.log;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final FileStorageService fileStorageService;

    private final ProductRepository productRepository;


    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    @Transactional
    public Product createProduct(ProductCreateDTO dto) {

        // 1. Save all uploaded images and collect their URLs
        List<String> savedImageUrls = new ArrayList<>();

        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            for (MultipartFile file : dto.getImages()) {
                if (file != null && !file.isEmpty()) {
                    try {
                        String fileUrl = fileStorageService.saveImage(file);
                        savedImageUrls.add(fileUrl);
                    } catch (Exception e) {
                        log.error("Failed to save image: {}", file.getOriginalFilename(), e);
                    }
                }
            }
        }

        Product product = Product.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .stock(dto.getStock())
                .category(dto.getCategory())
                .subCategory(dto.getSubCategory())
                .build();


        if (!savedImageUrls.isEmpty()) {
            product.setImageUrl(savedImageUrls.get(0));
            product.setExtraImages(savedImageUrls);
        } else {
            product.setImageUrl("/images/placeholder-product.jpg");
            product.setExtraImages(new ArrayList<>());
        }

        return productRepository.save(product);
    }


    @Transactional
    public Product updateProduct(Long id, ProductDTO dto) {
        // 1. Fetch existing product
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        List<String> savedImageUrls = new ArrayList<>();

        // 2. Update basic fields only if they are provided (non-null and valid)
        if (dto.getName() != null && !dto.getName().isBlank()) {
            product.setName(dto.getName().trim());
        }
        if (dto.getDescription() != null) {
            product.setDescription(dto.getDescription().trim());
        }
        if (dto.getPrice() > 0) {   // avoid updating with 0 unless intentional
            product.setPrice(dto.getPrice());
        }
        if (dto.getStock() >= 0) {
            product.setStock(dto.getStock());
        }
        if (dto.getCategory() != null && !dto.getCategory().isBlank()) {
            product.setCategory(dto.getCategory().trim());
        }
        if (dto.getSubCategory() != null) {
            product.setSubCategory(dto.getSubCategory().trim());
        }

        // 3. Handle image updates (only if new files are provided)
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {


            for (MultipartFile file : dto.getImages()) {
                if (file != null && !file.isEmpty()) {
                    try {
                        String fileUrl = fileStorageService.saveImage(file);
                        savedImageUrls.add(fileUrl);
                    } catch (Exception e) {
                        log.error("Failed to save image: {}", file.getOriginalFilename(), e);
                    }
                }
            }

        }
        // 4. Assign images properly
        if (!savedImageUrls.isEmpty()) {
            product.setImageUrl(savedImageUrls.get(0));

            // All images go to extraImages (including main one — common pattern)
            product.setExtraImages(savedImageUrls);
        } else {
            // Optional: fallback placeholder
            product.setImageUrl("/images/placeholder-product.jpg");
            product.setExtraImages(new ArrayList<>());
        }

        // 4. Save updated entity
        return productRepository.save(product);
    }


    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        productRepository.delete(product);
    }
}


package org.example.sbgroup2.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.sbgroup2.dto.ProductCreateDTO;
import org.example.sbgroup2.models.Product;
import org.example.sbgroup2.repositories.ProductRepository;
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

//    public ProductService(ProductRepository productRepository) {
//        this.productRepository = productRepository;
//    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }


    // we'll create this next

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
                        // Continue with next images — don't fail whole product creation
                    }
                }
            }
        }

        // 3. Build the Product entity
        Product product = Product.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .stock(dto.getStock())
                .category(dto.getCategory())
                .subCategory(dto.getSubCategory())
                .build();

        // 4. Assign images properly
        if (!savedImageUrls.isEmpty()) {
            // First image = main imageUrl
            product.setImageUrl(savedImageUrls.get(0));

            // All images go to extraImages (including main one — common pattern)
            product.setExtraImages(savedImageUrls);
        } else {
            // Optional: fallback placeholder
            product.setImageUrl("/images/placeholder-product.jpg");
            product.setExtraImages(new ArrayList<>());
        }

        return productRepository.save(product);
    }

    public List<Product> findByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    // 🔥 DELETE LOGIC
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        productRepository.delete(product);
    }
}


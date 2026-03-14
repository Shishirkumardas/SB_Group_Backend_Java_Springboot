package org.sb_ibms.controller;

import lombok.RequiredArgsConstructor;

import org.sb_ibms.models.Product;
import org.sb_ibms.repositories.ProductRepository;
import org.sb_ibms.services.ProductService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductRepository productRepository;


    @PostMapping
    public Product addProduct(@RequestBody Product product) {
        return productService.saveProduct(product);
    }

    @GetMapping
    public List<Product> getProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String subCategory) {
        if (category != null && subCategory != null) {
            return productRepository.findByCategoryAndSubCategory(category, subCategory);
        } else if (category != null) {
            return productRepository.findByCategory(category);
        }
        return productRepository.findAll();
    }

    @GetMapping("/{id}")
    public Product getProductById(@PathVariable Long id) {
        return productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
    }
}

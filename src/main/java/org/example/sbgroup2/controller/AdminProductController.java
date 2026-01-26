package org.example.sbgroup2.controller;

import lombok.RequiredArgsConstructor;

import org.example.sbgroup2.dto.ProductCreateDTO;
import org.example.sbgroup2.models.Product;
import org.example.sbgroup2.repositories.ProductRepository;
import org.example.sbgroup2.services.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")  // protect with role
public class AdminProductController {

    private final ProductService productService;
    private final ProductRepository productRepository;

    @PostMapping
    public ResponseEntity<Product> createProduct(
            @ModelAttribute ProductCreateDTO dto) {   // @ModelAttribute for multipart

        Product saved = productService.createProduct(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // 🔥 DELETE PRODUCT
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
    // In OrderService or AdminController

}

package org.sb_ibms.controller;

import lombok.RequiredArgsConstructor;

import org.sb_ibms.dto.ProductCreateDTO;
import org.sb_ibms.dto.ProductDTO;
import org.sb_ibms.models.Product;
import org.sb_ibms.repositories.ProductRepository;
import org.sb_ibms.services.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<Product> createProduct(
            @ModelAttribute ProductCreateDTO dto) {   // @ModelAttribute for multipart

        Product saved = productService.createProduct(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PostMapping("/{id}")
    @PatchMapping("/{id}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @ModelAttribute ProductDTO dto) {   // @ModelAttribute for multipart

        Product saved = productService.updateProduct(id,dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }


}

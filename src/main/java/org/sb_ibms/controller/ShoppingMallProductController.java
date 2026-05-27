package org.sb_ibms.controller;

import lombok.RequiredArgsConstructor;
import org.sb_ibms.models.ShoppingMallProduct;
import org.sb_ibms.services.ShoppingMallProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shoppingmall-products")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3001")
public class ShoppingMallProductController {

    private final ShoppingMallProductService productService;

    @PostMapping
    public ResponseEntity<ShoppingMallProduct> createProduct(@RequestBody ShoppingMallProduct product) {
        return ResponseEntity.ok(productService.createProduct(product));
    }

    @GetMapping
    public ResponseEntity<List<ShoppingMallProduct>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShoppingMallProduct> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/barcode")
    public ResponseEntity<ShoppingMallProduct> getProductByBarcode(@RequestParam String barcode) {
        return ResponseEntity.ok(productService.getProductByBarcode(barcode));
    }

    @GetMapping("/name")
    public ResponseEntity<ShoppingMallProduct> getProductByName(@RequestParam String name) {
        return ResponseEntity.ok(productService.getProductByName(name));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ShoppingMallProduct> updateProduct(
            @PathVariable Long id,
            @RequestBody ShoppingMallProduct product) {
        return ResponseEntity.ok(productService.updateProduct(id, product));
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<ShoppingMallProduct> updateStock(
            @PathVariable Long id,
            @RequestParam Integer stock) {
        return ResponseEntity.ok(productService.updateStock(id, stock));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.deleteProduct(id));
    }
}
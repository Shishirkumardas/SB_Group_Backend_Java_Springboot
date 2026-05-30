package org.sb_ibms.services;

import lombok.RequiredArgsConstructor;
import org.sb_ibms.models.ShoppingMallProduct;
import org.sb_ibms.repositories.ShoppingMallProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShoppingMallProductService {

    private final ShoppingMallProductRepository productRepository;
    private final ShoppingMallContext shoppingMallContext;

    // =========================================
    // CREATE PRODUCT
    // =========================================
    public ShoppingMallProduct createProduct(ShoppingMallProduct product) {
        Long mallId = shoppingMallContext.getCurrentMallId();

        // Set mall ID for new product
        product.setShoppingMallId(mallId);

        // Check duplicate barcode within the same mall
        productRepository.findByBarcodeAndShoppingMallId(product.getBarcode(), mallId)
                .ifPresent(p -> {
                    throw new RuntimeException(
                            "Product already exists with barcode: " + product.getBarcode()
                    );
                });

        return productRepository.save(product);
    }

    // =========================================
    // GET ALL PRODUCTS (Filtered by current mall)
    // =========================================
    public List<ShoppingMallProduct> getAllProducts() {
        Long mallId = shoppingMallContext.getCurrentMallId();

        if (mallId == null) {
            return productRepository.findAll();           // Admin sees everything
        }
        return productRepository.findByShoppingMallId(mallId);  // Manager sees only their mall
    }

    // =========================================
    // GET PRODUCT BY ID (with access check)
    // =========================================
    public ShoppingMallProduct getProductById(Long id) {
        ShoppingMallProduct product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + id));

        Long mallId = shoppingMallContext.getCurrentMallId();

        // Security check for managers
        if (mallId != null && !mallId.equals(product.getShoppingMallId())) {
            throw new RuntimeException("Access denied: This product belongs to another shopping mall");
        }

        return product;
    }

    // =========================================
    // GET PRODUCT BY BARCODE
    // =========================================
    public ShoppingMallProduct getProductByBarcode(String barcode) {
        Long mallId = shoppingMallContext.getCurrentMallId();

        return productRepository.findByBarcodeAndShoppingMallId(barcode, mallId)
                .orElseThrow(() -> new RuntimeException(
                        "Product not found with barcode: " + barcode
                ));
    }

    public List<ShoppingMallProduct> searchProducts(String query) {
        Long mallId = shoppingMallContext.getCurrentMallId();

        if (mallId == null) {
            // Admin sees everything
            return productRepository.searchByNameOrBarcode(query);
        }

        // Manager sees only their mall's products
        return productRepository.searchByNameOrBarcodeInMall(query, mallId);
    }

    // =========================================
    // GET PRODUCT BY NAME
    // =========================================
    public ShoppingMallProduct getProductByName(String name) {
        Long mallId = shoppingMallContext.getCurrentMallId();

        return productRepository.findByNameAndShoppingMallId(name, mallId)
                .orElseThrow(() -> new RuntimeException(
                        "Product not found with name: " + name
                ));
    }

    // =========================================
    // UPDATE PRODUCT
    // =========================================
    public ShoppingMallProduct updateProduct(Long id, ShoppingMallProduct updatedProduct) {
        ShoppingMallProduct existing = getProductById(id);   // This already checks permission

        existing.setBarcode(updatedProduct.getBarcode());
        existing.setName(updatedProduct.getName());
        existing.setPrice(updatedProduct.getPrice());
        existing.setDiscountPrice(updatedProduct.getDiscountPrice());
        existing.setStock(updatedProduct.getStock());
        existing.setCategory(updatedProduct.getCategory());

        return productRepository.save(existing);
    }

    // =========================================
    // DELETE PRODUCT
    // =========================================
    public String deleteProduct(Long id) {
        ShoppingMallProduct product = getProductById(id);   // Permission check

        productRepository.delete(product);
        return "Product deleted successfully";
    }

    // =========================================
    // STOCK UPDATE
    // =========================================
    public ShoppingMallProduct updateStock(Long id, Integer stock) {
        ShoppingMallProduct product = getProductById(id);   // Permission check

        product.setStock(stock);
        return productRepository.save(product);
    }
}
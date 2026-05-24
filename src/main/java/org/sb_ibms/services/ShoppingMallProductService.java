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

    // =========================================
    // CREATE PRODUCT
    // =========================================
    public ShoppingMallProduct createProduct(ShoppingMallProduct product) {

        // Check duplicate barcode
        productRepository.findByBarcode(product.getBarcode())
                .ifPresent(p -> {
                    throw new RuntimeException(
                            "Product already exists with barcode: "
                                    + product.getBarcode()
                    );
                });

        return productRepository.save(product);
    }

    // =========================================
    // GET ALL PRODUCTS
    // =========================================
    public List<ShoppingMallProduct> getAllProducts() {
        return productRepository.findAll();
    }

    // =========================================
    // GET PRODUCT BY ID
    // =========================================
    public ShoppingMallProduct getProductById(Long id) {

        return productRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Product not found with ID: " + id
                        )
                );
    }

    // =========================================
    // GET PRODUCT BY BARCODE
    // =========================================
    public ShoppingMallProduct getProductByBarcode(String barcode) {

        return productRepository.findByBarcode(barcode)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Product not found with barcode: "
                                        + barcode
                        )
                );
    }

    // =========================================
    // GET PRODUCT BY NAME
    // =========================================
    public ShoppingMallProduct getProductByName(String name) {

        return productRepository.findByName(name)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Product not found with name: "
                                        + name
                        )
                );
    }

    // =========================================
    // UPDATE PRODUCT
    // =========================================
    public ShoppingMallProduct updateProduct(
            Long id,
            ShoppingMallProduct updatedProduct
    ) {

        ShoppingMallProduct existing =
                getProductById(id);

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

        ShoppingMallProduct product =
                getProductById(id);

        productRepository.delete(product);

        return "Product deleted successfully";
    }

    // =========================================
    // STOCK UPDATE
    // =========================================
    public ShoppingMallProduct updateStock(
            Long id,
            Integer stock
    ) {

        ShoppingMallProduct product =
                getProductById(id);

        product.setStock(stock);

        return productRepository.save(product);
    }
}

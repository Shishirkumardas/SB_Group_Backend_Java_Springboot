package org.example.sbgroup2.controller;

import lombok.RequiredArgsConstructor;

import org.example.sbgroup2.dto.AddToCartRequest;
import org.example.sbgroup2.dto.UpdateCartRequest;
import org.example.sbgroup2.models.CartItem;
import org.example.sbgroup2.models.Product;
import org.example.sbgroup2.models.User;
import org.example.sbgroup2.repositories.CartItemRepository;
import org.example.sbgroup2.repositories.ProductRepository;
import org.example.sbgroup2.repositories.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    // ✅ Get cart of logged-in user
    @GetMapping
    public List<CartItem> getCart(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository
                .findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return cartItemRepository.findByUserId(user.getId());
    }

    // ✅ Add to cart (JWT-based)
    @PostMapping("/add")
    public CartItem addToCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody AddToCartRequest request
    ) {
        User user = userRepository
                .findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Product product = productRepository
                .findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        CartItem item = cartItemRepository
                .findByUserAndProduct(user, product)
                .orElse(CartItem.builder()
                        .user(user)
                        .product(product)
                        .quantity(0)
                        .price(product.getPrice())
                        .build());

        item.setQuantity(item.getQuantity() + request.getQuantity());

        return cartItemRepository.save(item);
    }

    // ✅ Update quantity
    @PutMapping("/update")
    public CartItem updateQuantity(
            @RequestBody UpdateCartRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        CartItem item = cartItemRepository.findById(String.valueOf(request.getItemId()))
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        item.setQuantity(request.getQuantity());
        return cartItemRepository.save(item);
    }

    // ✅ Remove item
    @DeleteMapping("/remove/{itemId}")
    public void removeItem(@PathVariable Long itemId) {
        cartItemRepository.deleteById(String.valueOf(itemId));
    }
}

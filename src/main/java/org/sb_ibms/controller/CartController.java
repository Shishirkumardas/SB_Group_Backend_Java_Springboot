package org.sb_ibms.controller;

import lombok.RequiredArgsConstructor;

import org.sb_ibms.dto.AddToCartRequest;
import org.sb_ibms.dto.UpdateCartRequest;
import org.sb_ibms.models.CartItem;
import org.sb_ibms.models.Product;
import org.sb_ibms.models.User;
import org.sb_ibms.repositories.CartItemRepository;
import org.sb_ibms.repositories.ProductRepository;
import org.sb_ibms.repositories.UserRepository;
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


    @GetMapping
    public List<CartItem> getCart(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository
                .findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return cartItemRepository.findByUserId(user.getId());
    }


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


    @DeleteMapping("/remove/{itemId}")
    public void removeItem(@PathVariable Long itemId) {
        cartItemRepository.deleteById(String.valueOf(itemId));
    }
}

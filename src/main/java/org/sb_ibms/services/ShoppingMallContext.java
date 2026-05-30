package org.sb_ibms.services;

import lombok.RequiredArgsConstructor;
import org.sb_ibms.models.CustomUserDetails;
import org.sb_ibms.models.User;
import org.sb_ibms.repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShoppingMallContext {

    private final CurrentMallService currentMallService;
    private final UserRepository userRepository;   // ← Add this

    public Long getCurrentMallId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }

        String role = getCurrentUserRole();

        if ("ADMIN".equalsIgnoreCase(role)) {
            return null;
        }

        if (isShoppingMallManager(role)) {
            Long sessionMallId = currentMallService.getCurrentMallId();

            // If no session selection, use default from users table
            if (sessionMallId == null) {
                String userId = getCurrentUserId();
                if (userId != null) {
                    User user = userRepository.findById(userId).orElse(null);
                    if (user != null && user.getShoppingMallId() != null) {
                        sessionMallId = user.getShoppingMallId();
                        currentMallService.setCurrentMall(sessionMallId); // cache in session
                        System.out.println("🔄 Using default mall from users table: " + sessionMallId);
                    }
                }
            }

            if (sessionMallId == null) {
                throw new RuntimeException("Please select a shopping mall first from 'My Malls' page.");
            }

            return sessionMallId;
        }

        return null;
    }

    private String getCurrentUserRole() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof CustomUserDetails user) {
            return user.getRole();
        }
        return null;
    }

    private String getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof CustomUserDetails user) {
            return user.getId();
        }
        return null;
    }

    private boolean isShoppingMallManager(String role) {
        if (role == null) return false;
        return "SHOPPING_MALL_MANAGER".equalsIgnoreCase(role) ||
                "SHOPPING_MALL_ASSISTANT".equalsIgnoreCase(role);
    }
}
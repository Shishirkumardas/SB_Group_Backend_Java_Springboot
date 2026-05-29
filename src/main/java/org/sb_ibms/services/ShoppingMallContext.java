package org.sb_ibms.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShoppingMallContext {

    private final CurrentMallService currentMallService;

    public Long getCurrentMallId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }

        String role = getCurrentUserRole();

        if ("ADMIN".equalsIgnoreCase(role)) {
            return null; // Admin sees all
        }

        if (isShoppingMallManager(role)) {
            return currentMallService.getCurrentMallId();
        }

        return null; // Other roles see nothing or everything (as per your logic)
    }

    private String getCurrentUserRole() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
            // Adjust according to your CustomUserDetails implementation
            return userDetails.getAuthorities().stream()
                    .findFirst()
                    .map(a -> a.getAuthority().replace("ROLE_", ""))
                    .orElse(null);
        }
        return null;
    }

    private boolean isShoppingMallManager(String role) {
        return "SHOPPING_MALL_MANAGER".equalsIgnoreCase(role) ||
                "MALL_MANAGER".equalsIgnoreCase(role);
    }
}
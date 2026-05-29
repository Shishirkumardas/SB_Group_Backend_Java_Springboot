package org.sb_ibms.controller;

import lombok.RequiredArgsConstructor;
import org.sb_ibms.dto.MallListDTO;
import org.sb_ibms.dto.MallSelectRequest;
import org.sb_ibms.dto.ShoppingMallRequest;
import org.sb_ibms.models.ShoppingMall;
import org.sb_ibms.services.CurrentMallService;
import org.sb_ibms.services.ShoppingMallContext;
import org.sb_ibms.services.ShoppingMallService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shopping-mall")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3001")
public class ShoppingMallController {

    private final ShoppingMallService shoppingMallService;
    private final CurrentMallService currentMallService;
    private final ShoppingMallContext shoppingMallContext;

    // ==================== CRUD Operations ====================

    @PostMapping
    public ResponseEntity<ShoppingMall> create(@RequestBody ShoppingMallRequest request) {
        String adminId = getCurrentUserId();
        ShoppingMall mall = shoppingMallService.createShoppingMall(request, adminId);
        return ResponseEntity.ok(mall);
    }

    @GetMapping
    public ResponseEntity<List<ShoppingMall>> getAll() {
        return ResponseEntity.ok(shoppingMallService.getAllShoppingMalls());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShoppingMall> getById(@PathVariable Long id) {
        return ResponseEntity.ok(shoppingMallService.getShoppingMallById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ShoppingMall> update(@PathVariable Long id,
                                               @RequestBody ShoppingMallRequest request) {
        return ResponseEntity.ok(shoppingMallService.updateShoppingMall(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        shoppingMallService.deleteShoppingMall(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== Mall Selection for Managers ====================

    @GetMapping("/my-malls")
    public ResponseEntity<List<MallListDTO>> getMyMalls() {
        List<MallListDTO> malls = shoppingMallService.getMallsForCurrentUser();
        return ResponseEntity.ok(malls);
    }

    @PostMapping("/select")
    public ResponseEntity<String> selectMall(@RequestBody MallSelectRequest request) {
        if (request.getShoppingMallId() == null) {
            return ResponseEntity.badRequest().body("Shopping mall ID is required");
        }

        String userId = getCurrentUserId();
        boolean allowed = shoppingMallService.isManagerAllowedForMall(userId, request.getShoppingMallId());

        if (!allowed) {
            return ResponseEntity.status(403).body("You are not authorized for this mall");
        }

        currentMallService.setCurrentMall(request.getShoppingMallId());
        return ResponseEntity.ok("Shopping mall selected successfully");
    }

    @PostMapping("/clear-selection")
    public ResponseEntity<String> clearSelection() {
        currentMallService.clearCurrentMall();
        return ResponseEntity.ok("Current mall selection cleared");
    }

    // ==================== Manager Assignment (Admin Only) ====================

    @PostMapping("/{mallId}/assign-manager")
    public ResponseEntity<String> assignManager(
            @PathVariable Long mallId,
            @RequestParam String email) {     // Changed to email

        String adminId = getCurrentUserId();
        shoppingMallService.assignManagerToMall(email, mallId, adminId);
        return ResponseEntity.ok("Manager assigned successfully");
    }

    // Helper method
    private String getCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getPrincipal() instanceof org.sb_ibms.models.CustomUserDetails user) {
            String idStr = user.getId();

            if (idStr == null || idStr.trim().isEmpty() || "null".equals(idStr)) {
                return null;
            }
            return idStr;
        }
        return null;
    }
}
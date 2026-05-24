package org.sb_ibms.controller;

import lombok.RequiredArgsConstructor;
import org.sb_ibms.dto.userDTO;
import org.sb_ibms.models.User;
import org.sb_ibms.repositories.UserRepository;
import org.sb_ibms.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(
        origins = "${frontend.origin:http://localhost:3001}",
        allowCredentials = "true",
        allowedHeaders = "*",
        exposedHeaders = "Set-Cookie"
)
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    // ====================== CREATE ======================
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DMD', 'PD', 'GM')")
    public ResponseEntity<User> createUser(@RequestBody userDTO dto) {
        User savedUser = userService.createUser(dto);
        return ResponseEntity.ok(savedUser);
    }

    // ====================== READ ======================
    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable String id) {
        User user = userService.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(user);
    }

    // NEW: Recursive Hierarchy with Subordinates (For Tree View)
    @GetMapping("/{id}/with-subordinates")
    public ResponseEntity<User> getUserWithSubordinates(@PathVariable String id) {
        User user = userService.getUserWithSubordinates(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DMD', 'PD', 'GM')")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{managerId}/subordinates")
    public ResponseEntity<List<User>> getSubordinates(@PathVariable String managerId) {
        return ResponseEntity.ok(userService.getSubordinates(managerId));
    }

    // ====================== UPDATE ======================
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DMD', 'PD', 'GM')")
    public ResponseEntity<User> updateUser(@PathVariable String id, @RequestBody userDTO dto) {
        User user = new User();
        user.setId(id);
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPhoneNumber(dto.getPhone());
        user.setRole(dto.getRole());
        User manager =userRepository.findById(dto.getManagerId())
                .orElseThrow(() -> new RuntimeException("Manager not found"));
        user.setManager(manager);  // For hierarchy update

        User updated = userService.updateUser(user);
        return ResponseEntity.ok(updated);
    }


    // ====================== PERFORMANCE UPDATE (Optional) ======================
    @PutMapping("/{id}/performance")
    @PreAuthorize("hasAnyRole('ADMIN', 'DMD', 'GM')")
    public ResponseEntity<Void> updatePerformance(
            @PathVariable String id,
            @RequestParam BigDecimal netSale,
            @RequestParam BigDecimal profit,
            @RequestParam BigDecimal commission) {

        userService.updatePerformanceMetrics(id, netSale, profit, commission);
        return ResponseEntity.ok().build();
    }

    // ====================== DELETE ======================
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DMD')")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {

        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
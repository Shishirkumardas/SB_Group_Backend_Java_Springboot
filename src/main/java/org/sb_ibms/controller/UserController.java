package org.sb_ibms.controller;
import lombok.RequiredArgsConstructor;
import org.sb_ibms.dto.userDTO;
import org.sb_ibms.models.User;
import org.sb_ibms.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DMD', 'PD', 'GM')")
    public ResponseEntity<User> createUser(@RequestBody userDTO dto) {
        User savedUser = userService.createUser(dto);
        return ResponseEntity.ok(savedUser);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable String id) {
        User user = userService.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
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

    @PutMapping("/update")
    public ResponseEntity<User> updateUser(@RequestBody User updatedUser) {
        if (updatedUser.getId() == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(userService.updateUser(updatedUser));
    }
}

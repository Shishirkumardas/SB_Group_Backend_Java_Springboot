package org.example.sbgroup2.controller;

import lombok.RequiredArgsConstructor;

import org.example.sbgroup2.models.User;
import org.example.sbgroup2.repositories.UserRepository;
import org.example.sbgroup2.services.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

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
    private final UserRepository userRepo;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;           // ← use interface
    private final AuthenticationManager authenticationManager;

    @GetMapping("/{id}")
    public User getUser(@PathVariable String id) {
        return userRepo.findById(id).orElseThrow();
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateUser(@RequestBody User updatedUser) {
        if (updatedUser.getId() == null) {
            return ResponseEntity.badRequest().body("User ID is required");
        }

        return userRepo.findById(updatedUser.getId())
                .map(existingUser -> {
                    // Only update allowed fields (never password here!)
                    if (updatedUser.getName() != null) {
                        existingUser.setName(updatedUser.getName());
                    }
                    if (updatedUser.getPhoneNumber() != null) {
                        existingUser.setPhoneNumber(updatedUser.getPhoneNumber());
                    }
                    if (updatedUser.getAddress() != null) {
                        existingUser.setAddress(updatedUser.getAddress());
                    }
                    // Add other safe fields if needed (email? → maybe with verification)

                    User saved = userRepo.save(existingUser);
                    return ResponseEntity.ok(saved);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Optional: more strict version with DTO
    // @PutMapping("/update")
//    @PutMapping("/update")
//    public ResponseEntity<?> updateProfile(@RequestBody UserUpdateDTO dto) {
//        if (dto.getId() == null) {
//            return ResponseEntity.badRequest().body("User ID is required");
//        }
//
//        return userRepo.findById(dto.getId())
//                .map(user -> {
//                    if (dto.getFullName() != null) user.setName(dto.getFullName());
//                    if (dto.getPhone()     != null) user.setPhone(dto.getPhone());
//                    if (dto.getAddress()   != null) user.setAddress(dto.getAddress());
//
//                    User saved = userRepo.save(user);
//                    return ResponseEntity.ok(saved);
//                })
//                .orElseGet(() -> ResponseEntity.notFound().build());
//    }
}

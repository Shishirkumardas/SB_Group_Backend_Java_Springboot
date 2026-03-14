package org.sb_ibms.controller;

import lombok.RequiredArgsConstructor;
import org.sb_ibms.models.User;
import org.sb_ibms.repositories.UserRepository;
import org.springframework.http.ResponseEntity;
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

                    if (updatedUser.getName() != null) {
                        existingUser.setName(updatedUser.getName());
                    }
                    if (updatedUser.getPhoneNumber() != null) {
                        existingUser.setPhoneNumber(updatedUser.getPhoneNumber());
                    }
                    if (updatedUser.getAddress() != null) {
                        existingUser.setAddress(updatedUser.getAddress());
                    }


                    User saved = userRepo.save(existingUser);
                    return ResponseEntity.ok(saved);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}

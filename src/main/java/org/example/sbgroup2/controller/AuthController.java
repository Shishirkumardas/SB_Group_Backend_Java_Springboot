package org.example.sbgroup2.controller;


import lombok.RequiredArgsConstructor;
import org.example.sbgroup2.dto.LoginRequest;
import org.example.sbgroup2.dto.SignupRequest;
import org.example.sbgroup2.enums.Role;
import org.example.sbgroup2.models.User;
import org.example.sbgroup2.repositories.UserRepository;
import org.example.sbgroup2.services.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "${frontend.origin:http://localhost:3001}", allowCredentials = "true")
@RequiredArgsConstructor
public class AuthController {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /* =========================
       SIGN UP
       ========================= */
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest req) {

        if (userRepo.existsByEmail(req.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Email already exists"));
        }

        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));

        // Default to CUSTOMER – never let client decide admin role!
        user.setRole(Role.CUSTOMER); // ← CHANGE: remove req.getRole() in production!

        userRepo.save(user);

        return ResponseEntity.ok(Map.of("message", "User registered successfully"));
    }

    /* =========================
       LOGIN
       ========================= */
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest req,
            HttpServletResponse response
    ) {
//        User user = userRepo.findByEmail(req.getEmail())
//                .orElseThrow(() -> new RuntimeException("Invalid credentials"));
        User user = userRepo.findByEmail(req.getEmail());
        if (user == null) {
            throw new RuntimeException("Invalid credentials");
        }

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Invalid pass"));
        }

        String token = jwtService.generateToken(user);

        Cookie cookie = new Cookie("token", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // set true in production (HTTPS)
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60); // 1 hour

        response.addCookie(cookie);

        return ResponseEntity.ok(
                Map.of(
                        "role", user.getRole().name(),
                        "email", user.getEmail()
                )
        );
    }

    /* =========================
       LOGOUT
       ========================= */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {

        Cookie cookie = new Cookie("token", "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setSecure(true);


        response.addCookie(cookie);

        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }

    /* =========================
       CURRENT USER
       ========================= */
//    @GetMapping("/me")
//    public ResponseEntity<?> me(Authentication authentication) {
//
//        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
//            return ResponseEntity.status(401).build();
//        }
//
//        return ResponseEntity.ok(
//                Map.of(
//                        "id", user.getId(),
//                        "email", user.getEmail(),
//                        "role", user.getRole().name()
//                )
//        );
//    }
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = (User) auth.getPrincipal();

        return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "role", user.getRole().name()
        ));
    }

}

package org.example.sbgroup2;


import lombok.RequiredArgsConstructor;
import org.example.sbgroup2.enums.Role;
import org.example.sbgroup2.models.User;
import org.example.sbgroup2.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    public void initAdmin() {

        if (userRepo.findByEmail("admin@sbgroup.com")==null) {

            User admin = new User();
            admin.setId(UUID.randomUUID().toString());
            admin.setEmail("admin@sbgroup.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);

            userRepo.save(admin);

            System.out.println("✅ Admin user created");
        }
    }
}


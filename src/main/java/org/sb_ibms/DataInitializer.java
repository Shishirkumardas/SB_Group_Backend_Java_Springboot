package org.sb_ibms;

import org.sb_ibms.enums.Role;
import org.sb_ibms.models.User;
import org.sb_ibms.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initUsers(UserRepository userRepository,
                                BCryptPasswordEncoder passwordEncoder) {
        return args -> {

            if (userRepository.findByEmail("admin@example.com").isEmpty()) {
                User admin = new User(
                        "admin@example.com",
                        passwordEncoder.encode("admin123"),
                        Role.ADMIN
                );
                userRepository.save(admin);
                System.out.println("✅ Admin user created");
            }

            if (userRepository.findByEmail("customer@example.com").isEmpty()) {
                User customer = new User(
                        "customer@example.com",
                        passwordEncoder.encode("customer123"),
                        Role.CUSTOMER
                );
                userRepository.save(customer);
                System.out.println("✅ Customer user created");
            }
        };
    }
}

package org.example.sbgroup2;

import org.example.sbgroup2.component.JwtFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(List.of("http://localhost:3001")); // ← your Next.js frontend
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setExposedHeaders(Arrays.asList("Authorization", "Set-Cookie")); // useful for debug

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
//                .csrf(csrf -> csrf.disable())
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                                .requestMatchers("/api/auth/login", "/api/auth/signup", "/api/auth/logout").permitAll()
                                .requestMatchers("/api/auth/me").authenticated()
                                .requestMatchers("/api/areas/**").permitAll()// or .authenticated() for testing
                                .requestMatchers("/api/master-data/**").permitAll()
                                .requestMatchers("/api/customer/**").permitAll()
                                .requestMatchers("/api/customer/submit").permitAll()
                                .requestMatchers("/api/cashback/**").permitAll()
                                .requestMatchers("/api/payments/**").permitAll()
                                .requestMatchers("/api/consumers/**").permitAll()
                                .requestMatchers("/api/summary/**").permitAll()
                                .requestMatchers("/api/daily-expenses/**").permitAll()
                                .requestMatchers("/api/accounts/**").permitAll()
                                .requestMatchers("/api/calls/**").permitAll()
                                .requestMatchers("/api/file-upload/**").permitAll()
                                .requestMatchers("/api/dashboard/**").permitAll()
//                                .requestMatchers(
//                                        "/api/areas/**",
//                                "/api/master-data/**",
//                                "/api/admin/**"
////                                "/api/users/**",                   // manage users
////                                "/api/reports/**",                 // admin reports
////                                "/api/settings/**"                 // system settings
//                                ).hasRole("ADMIN")
                                .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}

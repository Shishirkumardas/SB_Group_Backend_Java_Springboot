package org.example.sbgroup2;

import org.example.sbgroup2.component.JwtFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

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

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);

        // ← Add BOTH common Next.js ports + your actual one
        config.setAllowedOrigins(List.of(
                "http://localhost:3000",           // ← most common Next.js port
                "http://localhost:3001",           // ← your current one
                "http://192.168.68.107:3000",
                "http://192.168.68.107:3001",
                "http://172.16.0.2:3001",
                "https://unarticulate-unleached-tracey.ngrok-free.dev"
        ));

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowedHeaders(Arrays.asList("Content-Type", "Authorization", "token"));
        config.setExposedHeaders(Arrays.asList("Set-Cookie","Authorization"));
        // helpful for debugging

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        // Always allow preflight OPTIONS requests
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Public auth endpoints
                        .requestMatchers("/api/auth/login",
                                "/api/auth/signup",
                                "/api/auth/logout").permitAll()

                        // Protected but needs authentication
//                        .requestMatchers("/api/auth/me").authenticated()
                                // Protected but needs authentication
                                .requestMatchers("/api/auth/me").authenticated()
                                .requestMatchers("/api/auth/profile").authenticated()

//                        .requestMatchers("/api/customer/**").hasRole("CUSTOMER")


                        // For development – you can tighten later
                        .requestMatchers(
//                                "/api/areas/**",
                                "/api/master-data/**",
//                                "/api/customer/**",
//                                "/api/customer/submit",
                                "/api/cashback/**",
//                                "/api/payments/**",
                                "/api/consumers/**",
                                "/api/summary/**",
                                "/api/daily-expenses/**",
                                "/api/accounts/**",
                                "/api/calls/**",
                                "/api/file-upload/**",
//                                "/api/uploads/**",
                                "/api/dashboard/**",
                                "/api/dashboard/summary",
                                "/api/areas/area-summary/daily"
                        ).hasRole("ADMIN")
                        .requestMatchers("/api/areas/area").permitAll()
                        .requestMatchers("/api/payments/**").permitAll()
                        .requestMatchers("/api/customer/payment-methods").permitAll()
                                .requestMatchers("/api/customer/**").permitAll()
                        .requestMatchers("/api/areas").permitAll()
                                .requestMatchers("/api/products/**").permitAll()
                                .requestMatchers("/api/users/**").permitAll()
                                .requestMatchers("/api/cart/**").permitAll()
                                .requestMatchers("/api/uploads/**").permitAll()
                                .requestMatchers("/images/**").permitAll()
                                .requestMatchers("/uploads/**").permitAll()
                                .requestMatchers("/api/orders/**").permitAll()
                                .requestMatchers("/api/admin/products").hasRole("ADMIN")
                                .requestMatchers("/api/admin/products/**").hasRole("ADMIN")
                                .requestMatchers("/api/admin/orders/**").hasRole("ADMIN")
                                .requestMatchers("/api/admin/orders/products").hasRole("ADMIN")
                                .requestMatchers("/uploads/products/**").permitAll()
                                .requestMatchers("/api/bkash/**").permitAll()



                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )

                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
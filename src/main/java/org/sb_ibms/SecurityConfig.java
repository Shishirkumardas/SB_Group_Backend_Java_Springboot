package org.sb_ibms;

import org.sb_ibms.component.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
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

        config.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://localhost:3001",
                "http://192.168.68.107:3000",
                "http://192.168.68.107:3001",
                "http://172.16.0.2:3001",
                "https://unarticulate-unleached-tracey.ngrok-free.dev"
                // Add your production frontend URL here later (e.g. Vercel)
        ));

        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setExposedHeaders(Arrays.asList("Set-Cookie", "Authorization"));

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
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // ==================== PUBLIC AUTH ENDPOINTS ====================
                        .requestMatchers("/api/auth/**").permitAll()

                        // ==================== PUBLIC ENDPOINTS ====================
                        .requestMatchers("/api/shopping-mall-customer/submit").permitAll()
                        .requestMatchers("/api/payments/**").permitAll()
                        .requestMatchers("/api/customer/payment-methods").permitAll()
                        .requestMatchers("/api/customer/**").permitAll()           // if needed
                        .requestMatchers("/api/areas").permitAll()
                        .requestMatchers("/api/areas/area").permitAll()
                        .requestMatchers("/api/products/**").permitAll()
                        .requestMatchers("/api/bkash/**").permitAll()
                        .requestMatchers("/images/**", "/uploads/**", "/uploads/products/**").permitAll()

                        // ==================== ADMIN ONLY ====================
                        .requestMatchers(
                                "/api/master-data/**",
                                "/api/cashback/**",
                                "/api/consumers/**",
                                "/api/summary/**",
                                "/api/daily-expenses/**",
                                "/api/accounts/**",
                                "/api/calls/**",
                                "/api/file-upload/**",
                                "/api/dashboard/**",
                                "/api/areas/area-summary/daily",
                                "/api/admin/products",
                                "/api/admin/products/**",
                                "/api/admin/orders/**",
                                "/api/admin/orders/products"
                        ).hasRole("ADMIN")

                        // ==================== SHOPPING MALL MANAGER / ADMIN ====================
                        .requestMatchers(
                                "/api/shoppingmall-products/**",
                                "/api/shoppingmall-master-data/**",
                                "/api/shoppingMall-payments/**",
                                "/api/pos/**",
                                "/api/rewards/**",
                                "/api/shopping-mall-customer/**"
                        ).hasAnyRole("ADMIN", "SHOPPING_MALL_MANAGER")

                        // ==================== EVERYTHING ELSE REQUIRES AUTH ====================
                        .anyRequest().authenticated()
                )

                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
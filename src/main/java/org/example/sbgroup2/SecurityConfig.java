package org.example.sbgroup2;

import org.example.sbgroup2.component.JwtFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;


import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//
//        http
//                .cors(cors -> {})
//                // ✅ NEW CSRF SYNTAX
//                .csrf(csrf -> csrf.disable())
//
//                // ✅ Stateless JWT
//                .sessionManagement(session ->
//                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//                )
//
//                // ✅ Authorization rules
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/api/auth/**").permitAll()
//                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
//                        .requestMatchers("/api/customer/**").hasAnyRole("ADMIN", "CUSTOMER")
//                        .requestMatchers("/api/areas/**").hasRole("ADMIN")
//                        .requestMatchers("/api/master-data/**").hasRole("ADMIN")
//                        .anyRequest().authenticated()
//                )
//
//                // ✅ JWT filter
//                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
//
//        return http.build();
//    }
//    @Bean
//    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//                .csrf(csrf -> csrf.disable())
//                .cors(cors -> {})
//                .sessionManagement(session ->
//                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//                )
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers(
//                                "/api/auth/login",
//                                "/api/auth/signup"
//                        ).permitAll()
//
//                        .requestMatchers("/api/auth/me").authenticated()
//
//                        // ADMIN ONLY
//                        .requestMatchers("/api/areas/**").hasRole("ADMIN")
//
//                        .anyRequest().authenticated()
//                )
//
//
//                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
//
//
//        return http.build();
//    }
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowCredentials(true);
    config.addAllowedOrigin("http://localhost:3001"); // your frontend URL
    config.addAllowedHeader("*");
    config.addAllowedMethod("*");

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
}
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/login", "/api/auth/signup").permitAll()
                        .requestMatchers("/api/auth/me").authenticated()
                        .requestMatchers("/api/areas/**").permitAll() // or .authenticated() for testing
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
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http.csrf().disable()
//                .cors().and()
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/api/auth/me", "/api/areas/**").authenticated()
//                        .anyRequest().permitAll()
//                )
//                .sessionManagement().disable(); // since using JWT
//        return http.build();
//    }

//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        http
//                .cors() // 🔥 important
//                .and()
//                .csrf().disable()
//                .authorizeRequests()
//                .antMatchers("/api/auth/me").authenticated()
//                .anyRequest().permitAll()
//                .and()
//                .httpBasic(); // or JWT filter
//    }

}

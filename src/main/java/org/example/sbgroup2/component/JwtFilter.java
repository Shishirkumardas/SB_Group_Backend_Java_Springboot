package org.example.sbgroup2.component;

import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.http.HttpServletResponse;
import org.example.sbgroup2.models.User;
import org.example.sbgroup2.repositories.UserRepository;
import org.example.sbgroup2.services.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import java.util.List;




@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    JwtService jwtService;

    @Autowired
    UserRepository userRepo;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws IOException, ServletException {

        // ✅ GET REQUEST PATH CORRECTLY
        String path = request.getServletPath();

        // ✅ BYPASS JWT FILTER FOR PUBLIC ENDPOINTS
//        String path = request.getServletPath();

// ✅ Bypass ONLY truly public auth endpoints
        if (
                        path.equals("/api/auth/login") ||
                        path.equals("/api/auth/signup") ||
                        path.equals("/api/auth/logout") ||
                        path.startsWith("/api/file-upload/")
        ) {
            filterChain.doFilter(request, response);
            return;
        }


        String token = null;

        // ✅ READ JWT FROM COOKIE
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("token".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                Claims claims = jwtService.parse(token);

                User user = userRepo.findById(claims.getSubject())
                        .orElse(null);

                if (user != null) {
                    var authorities = List.of(
                            new SimpleGrantedAuthority("ROLE_"+user.getRole().name())
                    );


                    // Option 2: without prefix (if you prefer)
                    // var authorities = List.of(new SimpleGrantedAuthority(user.getRole().name()));

                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    user,           // principal
                                    null,           // credentials
                                    authorities
                            );

                    SecurityContextHolder.getContext().setAuthentication(auth);
                }

            } catch (Exception e) {
                logger.warn("JWT invalid or expired: " + e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
//    private String getTokenFromCookie(HttpServletRequest request) {
//        Cookie[] cookies = request.getCookies();
//        if (cookies != null) {
//            for (Cookie cookie : cookies) {
//                if ("token".equals(cookie.getName())) {
//                    return cookie.getValue();
//                }
//            }
//        }
//        return null;
//    }
}


package com.printpass.user.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. CSRF band karo — REST API hai, cookies nahi use karte
                .csrf(AbstractHttpConfigurer::disable)
                // 2. Session band karo — JWT use kar rahe hain, session ki zaroorat nahi
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // URL kaha jaraha apne Endpoints ke rules/permission define karo
                .authorizeHttpRequests(auth -> auth
                        // Rasta 1: Public and Internal Endpoints:
                                // PUBLIC — Koi bhi access kar sakta hai
                                .requestMatchers(HttpMethod.POST, "/api/v1/shops/register").permitAll()
                                // INTERNAL — Service to Service (Internal network only)
                                .requestMatchers("/api/v1/internal/**").permitAll()
                                .requestMatchers("/api/v1/otp/**").permitAll()   // ← OTP endpoints public

                        //  NEW RASTA: Swagger & API Docs (Development ke liye open)
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()


                        // Rasta 2: Only PRINT_SHOP endpoints
                                // {shopId}, my_shops ye saare shops ke baad aayenge
                                .requestMatchers("/api/v1/shops/**").hasRole("PRINT_SHOP")

                        // Rasta 3: Only ADMIN endpoints
                                // /api/v1/admin/ ke aage kuch bhi aaye, sirf ADMIN ko ijazat hai
                                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                        // Rasta 4: All other Requests  Baaki sab — Authenticate hona zaroori
                                .anyRequest().authenticated()
                )
                // 4. JWT Filter lagao — Spring Security ke default filter se pehle chalega
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();

    }
}
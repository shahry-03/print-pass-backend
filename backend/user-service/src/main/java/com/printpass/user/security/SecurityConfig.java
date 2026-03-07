package com.printpass.user.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.http.HttpMethod; // Yeh import laazmi add karein

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // Naya tarika: Lambda function se CSRF disable karna
                .csrf(AbstractHttpConfigurer::disable)

                // Naya tarika: Authorization rules lagana (Bina .and() ke)
                .authorizeHttpRequests(auth -> auth
                        // 🌟 NAYI LINE: Duniya ki tamam OPTIONS (Preflight) requests ko allow kardo!
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // /api/users/ ke tamaam rasto ko bina token ke allow karein
                        .requestMatchers("/api/users", "/api/users/login").permitAll()
                        // Baqi kisi bhi raste ke liye token lazmi ho (Security best practice)
                        .anyRequest().authenticated()
                )
                .build();
    }
}
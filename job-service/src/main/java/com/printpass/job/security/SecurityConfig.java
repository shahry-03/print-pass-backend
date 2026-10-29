package com.printpass.job.security;

import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. Disable CSRF
            .csrf(AbstractHttpConfigurer::disable)

            // 2. Enable CORS
            .cors(Customizer.withDefaults())

            // 3. Enforce Stateless Session Management
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // 4. Define Granular Access Rules
            .authorizeHttpRequests(auth -> auth
                    // Internal FORWARD aur ERROR dispatches ko allow karo taake 403 na bane
                    .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()

                    // ==========================================
                    // PUBLIC ENDPOINTS (SABSE PEHLE AAYENGE)
                    // ==========================================
                    // Upload aur Reprint bilkul public hain (no token required)
                    .requestMatchers(HttpMethod.POST, "/api/v1/jobs/upload", "/api/v1/jobs/upload/**").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/v1/jobs/reprint/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/jobs/status/**").permitAll() 
                    .requestMatchers("/error").permitAll()

                    // ==========================================
                    // DOCUMENTATION & MONITORING
                    // ==========================================
                    .requestMatchers(
                            "/v3/api-docs/**",
                            "/swagger-ui/**",
                            "/swagger-ui.html",
                            "/actuator/**"
                    ).permitAll()

                    // ==========================================
                    // PROTECTED ENDPOINTS (Sirf PRINT_SHOP ke liye)
                    // ==========================================
                    // Broad wildcard hata kar specific endpoints set kiye hain
                    .requestMatchers(HttpMethod.GET, "/api/v1/jobs/*").hasRole("PRINT_SHOP")
                    .requestMatchers(HttpMethod.POST, "/api/v1/jobs/*/print").hasRole("PRINT_SHOP")

                    // ==========================================
                    // FALLBACK RULE
                    // ==========================================
                    .anyRequest().authenticated()
            )

            // 5. Add the custom JWT filter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
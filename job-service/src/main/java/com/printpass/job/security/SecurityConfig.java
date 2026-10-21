package com.printpass.job.security;

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
            // 1. Disable CSRF (Cross-Site Request Forgery) since we are using stateless JWT authentication
            .csrf(AbstractHttpConfigurer::disable)

            // 2. Enable CORS (Cross-Origin Resource Sharing) to allow requests from the frontend
            .cors(Customizer.withDefaults())

            // 3. Enforce Stateless Session Management (No HttpSession will be created)
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // 4. Define Granular Access Rules for Endpoints
            .authorizeHttpRequests(auth -> auth
                    // ==========================================
                    // PUBLIC ENDPOINTS (No authentication required)
                    // ==========================================
                    // Allow students to upload files without login
                    .requestMatchers(HttpMethod.POST, "/api/v1/jobs/upload").permitAll()
                    // Allow students to reprint using an old PIN without login
                    .requestMatchers(HttpMethod.POST, "/api/v1/jobs/reprint/**").permitAll()

                    // ==========================================
                    // DOCUMENTATION & MONITORING
                    // ==========================================
                    // Allow access to Swagger UI and Actuator health checks
                    .requestMatchers(
                            "/v3/api-docs/**",
                            "/swagger-ui/**",
                            "/swagger-ui.html",
                            "/actuator/**"
                    ).permitAll()

                    // ==========================================
                    // PROTECTED ENDPOINTS (Authentication required)
                    // ==========================================
                    // Only authenticated shopkeepers (PRINT_SHOP role) can access job details and print actions
                    .requestMatchers("/api/v1/jobs/**").hasRole("PRINT_SHOP")

                    // ==========================================
                    // FALLBACK RULE
                    // ==========================================
                    // Any other unlisted endpoint requires the user to be authenticated
                    .anyRequest().authenticated()
            )

            // 5. Add the custom JWT filter before the default Spring Security UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
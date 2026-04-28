package com.printpass.authservice.service.impl;

import com.printpass.authservice.dto.request.LoginRequest;
import com.printpass.authservice.dto.response.InternalUserResponse;
import com.printpass.authservice.dto.response.LoginResponse;
import com.printpass.authservice.exception.InvalidCredentialsException;
import com.printpass.authservice.exception.UserNotActiveException;
import com.printpass.authservice.feign.UserServiceClient;
import com.printpass.authservice.security.JwtService;
import com.printpass.authservice.service.AuthService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserServiceClient userServiceClient;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginResponse login(LoginRequest request) {

        // ==========================================
        // STEP 1: User Service se user data lo
        // ==========================================
        InternalUserResponse user;
        try {
            user = userServiceClient.getUserByEmail(request.getEmail());
        } catch (FeignException.NotFound e) {
            // User exist nahi karta — lekin security ke liye same error dena
            log.warn("Login attempt with non-existent email: {}", request.getEmail());
            throw new InvalidCredentialsException("Invalid email or password!");
        } catch (FeignException e) {
            // User Service down hai ya koi aur masla
            log.error("User Service call failed: {}", e.getMessage());
            throw new RuntimeException("Service unavailable, please try again later!");
        }

        // ==========================================
        // STEP 2: User active hai ya nahi?
        // ==========================================
        if (!user.isActive()) {
            log.warn("Blocked user tried to login: {}", request.getEmail());
            throw new UserNotActiveException("Account not active. Please verify your email via OTP to continue.");
        }

        // ==========================================
        // STEP 3: Password check karo (BCrypt)
        // ==========================================
        boolean passwordMatch = passwordEncoder.matches(
                request.getPassword(),  // ← Plain text (jo user ne diya)
                user.getPassword()      // ← Hashed (jo database mein hai)
        );

        if (!passwordMatch) {
            log.warn("Wrong password for email: {}", request.getEmail());
            throw new InvalidCredentialsException("Invalid email or password!");
        }

        // ==========================================
        // STEP 4: JWT Token banao
        // ==========================================
        String token = jwtService.generateToken(
                user.getEmail(),
                user.getId().toString(),  // UUID → String
                user.getRole()
        );

        log.info("Login successful for: {}", request.getEmail());

        // ==========================================
        // STEP 5: LoginResponse return karo
        // ==========================================
        return LoginResponse.builder()
                .token(token)
                .role(user.getRole())
                .email(user.getEmail())
                .expiresIn(jwtService.getExpirationTime())
                .build();
    }
}
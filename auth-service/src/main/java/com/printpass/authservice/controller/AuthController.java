package com.printpass.authservice.controller;

import com.printpass.authservice.dto.request.LoginRequest;
import com.printpass.authservice.dto.response.LoginResponse;
import com.printpass.authservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    // ✅ Public — Koi bhi login kar sakta hai
    // Print Shop Owner ya Admin
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        log.debug("Login request received for email: {}", request.getEmail());

        LoginResponse response = authService.login(request);

        return ResponseEntity.ok(response);
    }
}
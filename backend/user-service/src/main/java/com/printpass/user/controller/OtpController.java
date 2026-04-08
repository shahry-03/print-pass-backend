package com.printpass.user.controller;

import com.printpass.user.dto.request.OtpRequest;
import com.printpass.user.dto.request.OtpVerifyRequest;
import com.printpass.user.service.OtpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/otp")
@RequiredArgsConstructor
public class OtpController {

    private final OtpService otpService;

    @PostMapping("/verify")
    public ResponseEntity<Void> verifyOtp(@Valid @RequestBody OtpVerifyRequest request) {
        otpService.verifyOtp(request.getEmail(), request.getOtp());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/resend")
    public ResponseEntity<Void> resendOtp(@Valid @RequestBody OtpRequest request) {
        otpService.resendOtp(request.getEmail());  // ← new method
        return ResponseEntity.ok().build();
    }
}
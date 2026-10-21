package com.printpass.user.service;

public interface OtpService {
    void sendOtpAsync(String email);
    void verifyOtp(String email, String otp);
    void resendOtp(String email);
}
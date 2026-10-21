package com.printpass.user.service;

public interface EmailService {
    void sendOtpEmail(String email, String otp);
}
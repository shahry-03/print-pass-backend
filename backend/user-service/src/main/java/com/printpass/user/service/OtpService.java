package com.printpass.user.service;

import com.printpass.user.entity.EmailVerificationToken;
import com.printpass.user.entity.User;
import com.printpass.user.exception.InvalidOtpException;
import com.printpass.user.exception.ResourceNotFoundException;
import com.printpass.user.repository.EmailVerificationTokenRepository;
import com.printpass.user.repository.UserRepository;

// Resend SDK Imports
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;

    // Purana JavaMailSender aur fromEmail hata diya, ab direct Resend API key aayegi
    @Value("${resend.api.key}")
    private String resendApiKey;

    private static final SecureRandom random = new SecureRandom();

    // 🔑 6-Digit OTP Generator
    private String generateOtp() {
        return String.format("%06d", random.nextInt(1000000));
    }

    // 🔥 ASYNC: Background mein chalega taake API fauran response de
    @Async
    @Transactional // Yeh async hone ki wajah se apni nayi independent transaction banayega
    public void sendOtpAsync(String email) {
        log.info("🚀 DEBUG: sendOtpAsync trigger ho gaya for email: {}", email); // 👈 Yeh line add karein
        log.debug("Starting async OTP send to {}", email);

        // 1. Purane unused tokens delete karo taake database clean rahe
        tokenRepository.deleteByEmail(email);

        // 2. Naya OTP banao aur database mein save karo
        String otp = generateOtp();
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(10); // 10 minutes limit

        EmailVerificationToken token = EmailVerificationToken.builder()
                .email(email)
                .otp(otp)
                .expiryDate(expiry)
                .used(false)
                .build();

        tokenRepository.save(token);

        // 3. Resend SDK Setup & Payload
        Resend resend = new Resend(resendApiKey);
        CreateEmailOptions params = CreateEmailOptions.builder()
                .from("PrintPass <onboarding@resend.dev>") // ⚠️ Testing ke waqt yahi rakhna hai!
                .to(email) // ⚠️ Testing me yahan sirf apka Resend account email hi hona chahiye
                .subject("PrintPass Email Verification")
                .html("<div style='font-family: Arial, sans-serif; text-align: center; padding: 20px;'>" +
                        "<h2>Welcome to PrintGrid! 🖨️</h2>" +
                        "<p>Your OTP for email verification is:</p>" +
                        "<h1 style='color: #4F46E5; letter-spacing: 5px; font-size: 32px;'>" + otp + "</h1>" +
                        "<p>This OTP is valid for 10 minutes. Please do not share it with anyone.</p></div>")
                .build();

        // 4. Bhejo aur Error Handle karo
        try {
            CreateEmailResponse data = resend.emails().send(params);
            log.info("✅ OTP sent successfully to {} in background via Resend (ID: {})", email, data.getId());
        } catch (ResendException e) {
            // 🛡️ SECURITY MOVE: Agar email fail ho, toh exception throw mat karo!
            // Warna user ka register process rollback ho jayega.
            // User baad mein 'Resend OTP' click karke dobara mangwa sakta hai.
            log.error("❌ Failed to send OTP to {}: {}", email, e.getMessage());
        }
    }

    // 🛡️ VERIFY: User ko verify karke active karne ka logic
    @Transactional
    public void verifyOtp(String email, String otp) {
        // 1. Token dhoondo
        EmailVerificationToken token = tokenRepository.findByEmailAndOtpAndUsedFalse(email, otp)
                .orElseThrow(() -> new InvalidOtpException("Invalid or expired OTP. Please check your email."));

        // 2. Expiry check karo
        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new InvalidOtpException("OTP has expired. Please request a new one.");
        }

        // 3. Token ko used mark karo taake dobara use na ho
        token.setUsed(true);
        tokenRepository.save(token);

        // 4. Asal User ko Active aur Verified mark karo
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setEmailVerified(true);
        user.setActive(true);
        userRepository.save(user);

        log.info("✅ User {} has been successfully verified and activated!", email);
    }

    // Resend OTP with validation — user exists and not already verified
    @Transactional
    public void resendOtp(String email) {
        // Check if user exists
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        // If already verified, don't send OTP again
        if (user.isEmailVerified()) {
            throw new RuntimeException("Email already verified. Please login.");
        }

        // Send OTP asynchronously
        sendOtpAsync(email);
    }
}
package com.printpass.user.service;

import com.printpass.user.entity.EmailVerificationToken;
import com.printpass.user.entity.User;
import com.printpass.user.exception.InvalidOtpException;
import com.printpass.user.exception.ResourceNotFoundException;
import com.printpass.user.repository.EmailVerificationTokenRepository;
import com.printpass.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
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
    private final JavaMailSender mailSender;
    private final UserRepository userRepository;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private static final SecureRandom random = new SecureRandom();

    // 🔑 6-Digit OTP Generator
    private String generateOtp() {
        return String.format("%06d", random.nextInt(1000000));
    }

    // 🔥 ASYNC: Background mein chalega taake API fauran response de
    @Async
    @Transactional // Yeh async hone ki wajah se apni nayi independent transaction banayega
    public void sendOtpAsync(String email) {
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

        // 3. Email tayyar karo
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("PrintPass Email Verification");
        message.setText("Welcome to PrintGrid! \n\nYour OTP for email verification is: " + otp + "\n\nThis OTP is valid for 10 minutes. Please do not share it with anyone.");
        message.setFrom(fromEmail);

        // 4. Bhejo aur Error Handle karo
        try {
            mailSender.send(message);
            log.info("✅ OTP sent successfully to {} in background", email);
        } catch (Exception e) {
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
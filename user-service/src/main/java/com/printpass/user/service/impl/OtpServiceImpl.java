package com.printpass.user.service.impl;

import com.printpass.user.entity.EmailVerificationToken;
import com.printpass.user.entity.User;
import com.printpass.user.exception.InvalidOtpException;
import com.printpass.user.exception.ResourceNotFoundException;
import com.printpass.user.repository.EmailVerificationTokenRepository;
import com.printpass.user.repository.UserRepository;
import com.printpass.user.service.EmailService;
import com.printpass.user.service.OtpService;
import com.printpass.user.utils.OtpGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpServiceImpl implements OtpService {

    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    // Hardcoded 10 minutes ko configurable bana diya
    @Value("${otp.expiration.minutes:10}")
    private int otpExpirationMinutes;

    @Override
    @Transactional
    public void sendOtpAsync(String email) {
        log.info("📢 Initial OTP generation started for: {}", email);

        // 1. Purane tokens delete karein
        tokenRepository.deleteByEmail(email);

        // 2. Naya OTP generate karke save karein (Utility class use ki)
        String otp = OtpGenerator.generateSixDigitOtp();
        saveOtpToken(email, otp);

        // 3. Email bhejein
        emailService.sendOtpEmail(email, otp);
    }

    @Override
    @Transactional
    public void verifyOtp(String email, String otp) {
        // 1. Token dhoondein
        EmailVerificationToken token = tokenRepository.findByEmailAndOtpAndUsedFalse(email, otp)
                .orElseThrow(() -> new InvalidOtpException("Invalid or expired OTP. Please check your email."));

        // 2. Expiry check karein
        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new InvalidOtpException("OTP has expired. Please request a new one.");
        }

        // 3. Token ko used mark karein
        token.setUsed(true);
        tokenRepository.save(token);

        // 4. User ko active karein
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        user.setEmailVerified(true);
        user.setActive(true);
        userRepository.save(user);

        log.info("✅ User {} has been successfully verified and activated!", email);
    }

    @Override
    @Transactional
    public void resendOtp(String email) {
        // 1. User check karein
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        // 2. Agar pehle se verified hai toh OTP mat bhejein
        if (user.isEmailVerified()) {
            throw new IllegalStateException("Email already verified. Please login."); // RuntimeException se behtar hai
        }

        // 3. Naya OTP generate karke save karein
        tokenRepository.deleteByEmail(email);
        String otp = OtpGenerator.generateSixDigitOtp();
        saveOtpToken(email, otp);

        // 4. Email bhejein
        emailService.sendOtpEmail(email, otp);
        log.info("📢 OTP resend process complete, handoff to EmailService for {}", email);
    }

    /**
     * Helper method taake DB save karne ka code repeat na ho (DRY Principle)
     */
    private void saveOtpToken(String email, String otp) {
        EmailVerificationToken token = EmailVerificationToken.builder()
                .email(email)
                .otp(otp)
                .expiryDate(LocalDateTime.now().plusMinutes(otpExpirationMinutes))
                .used(false)
                .build();
        tokenRepository.save(token);
    }
}
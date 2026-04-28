package com.printpass.user.service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    @Value("${resend.api.key}")
    private String resendApiKey;

    @Async // 🚀 Ab yeh 100% async chalega!
    public void sendOtpEmail(String email, String otp) {
        log.info("🚀 DEBUG: EmailService.sendOtpEmail trigger ho gaya for: {}", email);

        Resend resend = new Resend(resendApiKey);
        CreateEmailOptions params = CreateEmailOptions.builder()
                .from("PrintPass <onboarding@resend.dev>")
                .to(email)
                .subject("PrintPass Email Verification")
                .html("<h2>Your OTP is: " + otp + "</h2>") // Aapka styling wala HTML yahan aayega
                .build();

        try {
            resend.emails().send(params);
            log.info("✅ SUCCESS: Email sent to {}", email);
        } catch (ResendException e) {
            log.error("❌ ERROR: Resend failed: {}", e.getMessage());
        }
    }
}
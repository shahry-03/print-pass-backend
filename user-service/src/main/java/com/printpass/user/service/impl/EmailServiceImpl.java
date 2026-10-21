package com.printpass.user.service.impl;

import com.printpass.user.service.EmailService;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final Resend resend; // Ab yeh Config se inject ho raha hai

    @Value("${resend.from-email:PrintPass <onboarding@resend.dev>}")
    private String fromEmail;

    @Value("${resend.subject:PrintPass Email Verification}")
    private String emailSubject;

    @Override
    @Async
    public void sendOtpEmail(String email, String otp) {
        log.info("📧 Initiating email dispatch for: {}", email);

        // Professional HTML Template
        String htmlContent = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 10px;">
                    <div style="text-align: center;">
                        <h1 style="color: #4F46E5;">PrintPass</h1>
                        <h2 style="color: #333;">Email Verification</h2>
                    </div>
                    <p style="color: #555; font-size: 16px;">Hello,</p>
                    <p style="color: #555; font-size: 16px;">Thank you for using PrintPass. Please use the following One-Time Password (OTP) to verify your email address:</p>
                    
                    <div style="text-align: center; margin: 30px 0;">
                        <span style="font-size: 32px; font-weight: bold; letter-spacing: 5px; color: #4F46E5; background-color: #F3F4F6; padding: 10px 20px; border-radius: 8px;">
                            %s
                        </span>
                    </div>
                    
                    <p style="color: #555; font-size: 14px;">This OTP is valid for <strong>10 minutes</strong>. Please do not share this code with anyone.</p>
                    <p style="color: #999; font-size: 12px; margin-top: 30px; text-align: center;">If you didn't request this, you can safely ignore this email.</p>
                </div>
                """.formatted(otp);

        CreateEmailOptions params = CreateEmailOptions.builder()
                .from(fromEmail)
                .to(email)
                .subject(emailSubject)
                .html(htmlContent)
                .build();

        try {
            resend.emails().send(params);
            log.info("✅ SUCCESS: OTP Email successfully sent to {}", email);
        } catch (ResendException e) {
            log.error("❌ ERROR: Failed to send email via Resend to {}. Reason: {}", email, e.getMessage());
            // Production mein yahan aap exception throw kar sakte hain ya retry queue mein daal sakte hain
        }
    }
}
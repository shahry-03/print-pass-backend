package com.printpass.user.service.impl;

import com.printpass.user.entity.EmailVerificationToken;
import com.printpass.user.entity.User;
import com.printpass.user.exception.InvalidOtpException;
import com.printpass.user.exception.ResourceNotFoundException;
import com.printpass.user.repository.EmailVerificationTokenRepository;
import com.printpass.user.repository.UserRepository;
import com.printpass.user.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpServiceImplTest {

    @Mock
    private EmailVerificationTokenRepository tokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private OtpServiceImpl otpService;

    private final String testEmail = "test@printpass.com";

    @BeforeEach
    void setUp() {
        // Inject the @Value int field
        ReflectionTestUtils.setField(otpService, "otpExpirationMinutes", 10);
    }

    // ==========================================
    // TESTS FOR sendOtpAsync
    // ==========================================
    @Test
    void sendOtpAsync_ShouldDeleteOldTokens_SaveNewToken_AndSendEmail() {
        // Act
        otpService.sendOtpAsync(testEmail);

        // Assert
        verify(tokenRepository, times(1)).deleteByEmail(testEmail);

        ArgumentCaptor<EmailVerificationToken> tokenCaptor = ArgumentCaptor.forClass(EmailVerificationToken.class);
        verify(tokenRepository, times(1)).save(tokenCaptor.capture());

        EmailVerificationToken savedToken = tokenCaptor.getValue();
        assertEquals(testEmail, savedToken.getEmail());
        assertNotNull(savedToken.getOtp());
        assertFalse(savedToken.isUsed());

        verify(emailService, times(1)).sendOtpEmail(eq(testEmail), anyString());
    }

    // ==========================================
    // TESTS FOR verifyOtp
    // ==========================================
    @Test
    void verifyOtp_ShouldActivateUser_WhenOtpIsValid() {
        // Arrange
        EmailVerificationToken token = EmailVerificationToken.builder()
                .email(testEmail)
                .otp("123456")
                .expiryDate(LocalDateTime.now().plusMinutes(5))
                .used(false)
                .build();

        User user = User.builder()
                .email(testEmail)
                .isActive(false)
                .emailVerified(false)
                .build();

        when(tokenRepository.findByEmailAndOtpAndUsedFalse(testEmail, "123456")).thenReturn(Optional.of(token));
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(user));

        // Act
        otpService.verifyOtp(testEmail, "123456");

        // Assert
        assertTrue(token.isUsed());
        assertTrue(user.isActive());
        assertTrue(user.isEmailVerified());

        verify(tokenRepository, times(1)).save(token);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void verifyOtp_ShouldThrowException_WhenOtpIsInvalidOrNotFound() {
        // Arrange
        when(tokenRepository.findByEmailAndOtpAndUsedFalse(testEmail, "wrong_otp")).thenReturn(Optional.empty());

        // Act & Assert
        InvalidOtpException exception = assertThrows(InvalidOtpException.class, () -> {
            otpService.verifyOtp(testEmail, "wrong_otp");
        });
        assertTrue(exception.getMessage().contains("Invalid or expired OTP"));
    }

    @Test
    void verifyOtp_ShouldThrowException_WhenOtpIsExpired() {
        // Arrange
        EmailVerificationToken expiredToken = EmailVerificationToken.builder()
                .email(testEmail)
                .otp("123456")
                .expiryDate(LocalDateTime.now().minusMinutes(1)) // Expired 1 min ago
                .used(false)
                .build();

        when(tokenRepository.findByEmailAndOtpAndUsedFalse(testEmail, "123456")).thenReturn(Optional.of(expiredToken));

        // Act & Assert
        InvalidOtpException exception = assertThrows(InvalidOtpException.class, () -> {
            otpService.verifyOtp(testEmail, "123456");
        });
        assertTrue(exception.getMessage().contains("OTP has expired"));
        verify(tokenRepository, never()).save(any()); // Ensure it didn't save the expired token as used
    }

    // ==========================================
    // TESTS FOR resendOtp
    // ==========================================
    @Test
    void resendOtp_ShouldSendEmail_WhenUserExistsAndNotVerified() {
        // Arrange
        User user = User.builder()
                .email(testEmail)
                .emailVerified(false)
                .build();
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(user));

        // Act
        otpService.resendOtp(testEmail);

        // Assert
        verify(tokenRepository, times(1)).deleteByEmail(testEmail);
        verify(tokenRepository, times(1)).save(any(EmailVerificationToken.class));
        verify(emailService, times(1)).sendOtpEmail(eq(testEmail), anyString());
    }

    @Test
    void resendOtp_ShouldThrowException_WhenUserIsAlreadyVerified() {
        // Arrange
        User user = User.builder()
                .email(testEmail)
                .emailVerified(true) // Already verified
                .build();
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(user));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            otpService.resendOtp(testEmail);
        });
        assertEquals("Email already verified. Please login.", exception.getMessage());
        verify(emailService, never()).sendOtpEmail(anyString(), anyString());
    }

    @Test
    void resendOtp_ShouldThrowException_WhenUserDoesNotExist() {
        // Arrange
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            otpService.resendOtp(testEmail);
        });
    }
}
package com.printpass.user.service.impl;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.Emails;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private Resend resend;

    @Mock
    private Emails emails;

    @InjectMocks
    private EmailServiceImpl emailService;

    private final String testEmail = "test@printpass.com";
    private final String testOtp = "123456";
    private final String fromEmail = "support@send.printpass.tech";

    @BeforeEach
    void setUp() {
        // Inject @Value fields manually since we are not starting Spring Context
        ReflectionTestUtils.setField(emailService, "fromEmail", fromEmail);
        ReflectionTestUtils.setField(emailService, "emailSubject", "Test Subject");
    }

    @Test
    void sendOtpEmail_ShouldSendEmailWithCorrectHtml_WhenSuccessful() throws ResendException {
        // Arrange
        when(resend.emails()).thenReturn(emails);
        when(emails.send(any(CreateEmailOptions.class))).thenReturn(new CreateEmailResponse());

        // Act
        emailService.sendOtpEmail(testEmail, testOtp);

        // Assert
        ArgumentCaptor<CreateEmailOptions> optionsCaptor = ArgumentCaptor.forClass(CreateEmailOptions.class);
        verify(emails, times(1)).send(optionsCaptor.capture());

        CreateEmailOptions sentOptions = optionsCaptor.getValue();
        assertEquals(fromEmail, sentOptions.getFrom());
        assertEquals(testEmail, sentOptions.getTo().get(0));
        assertTrue(sentOptions.getHtml().contains(testOtp)); // Check if OTP is in the HTML
        assertTrue(sentOptions.getHtml().contains("PrintPass")); // Check if brand name is there
    }

    @Test
    void sendOtpEmail_ShouldHandleResendExceptionGracefully() throws ResendException {
        // Arrange
        when(resend.emails()).thenReturn(emails);
        when(emails.send(any(CreateEmailOptions.class))).thenThrow(new ResendException("API Error"));

        // Act & Assert
        // Verify that the method does not throw the exception (it catches it internally)
        assertDoesNotThrow(() -> emailService.sendOtpEmail(testEmail, testOtp));

        verify(emails, times(1)).send(any(CreateEmailOptions.class));
    }
}
package com.printpass.authservice.service.impl;

import com.printpass.authservice.dto.request.LoginRequest;
import com.printpass.authservice.dto.response.InternalUserResponse;
import com.printpass.authservice.dto.response.LoginResponse;
import com.printpass.authservice.exception.InvalidCredentialsException;
import com.printpass.authservice.exception.UserNotActiveException;
import com.printpass.authservice.feign.UserServiceClient;
import com.printpass.authservice.security.JwtService;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private LoginRequest loginRequest;
    private InternalUserResponse mockUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@printpass.com");
        loginRequest.setPassword("plainPassword123");

        mockUser = InternalUserResponse.builder()
                .id(userId)
                .email("test@printpass.com")
                .password("hashedPassword")
                .role("PRINT_SHOP")
                .isActive(true)
                .build();
    }

    // ==========================================
    // TEST 1: Successful Login
    // ==========================================
    @Test
    void login_ShouldReturnToken_WhenCredentialsAreValid() {
        // Arrange
        when(userServiceClient.getUserByEmail(anyString())).thenReturn(mockUser);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtService.generateToken(anyString(), anyString(), anyString())).thenReturn("mock.jwt.token");
        when(jwtService.getExpirationTime()).thenReturn(86400000L);

        // Act
        LoginResponse response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("mock.jwt.token", response.getToken());
        assertEquals("test@printpass.com", response.getEmail());
        assertEquals("PRINT_SHOP", response.getRole());
        
        verify(userServiceClient, times(1)).getUserByEmail(loginRequest.getEmail());
        verify(passwordEncoder, times(1)).matches(loginRequest.getPassword(), mockUser.getPassword());
        verify(jwtService, times(1)).generateToken(mockUser.getEmail(), userId.toString(), mockUser.getRole());
    }

    // ==========================================
    // TEST 2: User Not Found (Security Bypass)
    // ==========================================
    @Test
    void login_ShouldThrowInvalidCredentialsException_WhenUserDoesNotExist() {
        // Arrange
        // FeignException.NotFound simulate karne ke liye mock
        FeignException.NotFound notFoundException = mock(FeignException.NotFound.class);
        when(userServiceClient.getUserByEmail(anyString())).thenThrow(notFoundException);

        // Act & Assert
        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class, () -> {
            authService.login(loginRequest);
        });

        // Verify same error message as wrong password to prevent email enumeration
        assertEquals("Invalid email or password!", exception.getMessage());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    // ==========================================
    // TEST 3: User Service Down (Generic Feign Error)
    // ==========================================
    @Test
    void login_ShouldThrowRuntimeException_WhenUserServiceIsDown() {
        // Arrange
        FeignException feignException = mock(FeignException.class);
        when(userServiceClient.getUserByEmail(anyString())).thenThrow(feignException);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("Service unavailable, please try again later!", exception.getMessage());
    }

    // ==========================================
    // TEST 4: User is Inactive
    // ==========================================
    @Test
    void login_ShouldThrowUserNotActiveException_WhenUserIsInactive() {
        // Arrange
        mockUser.setActive(false); // User ko inactive kar dein
        when(userServiceClient.getUserByEmail(anyString())).thenReturn(mockUser);

        // Act & Assert
        UserNotActiveException exception = assertThrows(UserNotActiveException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("Account not active. Please verify your email via OTP to continue.", exception.getMessage());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    // ==========================================
    // TEST 5: Wrong Password
    // ==========================================
    @Test
    void login_ShouldThrowInvalidCredentialsException_WhenPasswordIsWrong() {
        // Arrange
        when(userServiceClient.getUserByEmail(anyString())).thenReturn(mockUser);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false); // Password match fail

        // Act & Assert
        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("Invalid email or password!", exception.getMessage());
        verify(jwtService, never()).generateToken(anyString(), anyString(), anyString());
    }
}
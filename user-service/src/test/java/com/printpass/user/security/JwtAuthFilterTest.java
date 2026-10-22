package com.printpass.user.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthFilter jwtAuthFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext(); // Clear context before each test
    }

    @Test
    void doFilterInternal_ShouldProceedWithoutAuth_WhenNoAuthHeader() throws Exception {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(null);

        // Act
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain, times(1)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_ShouldProceedWithoutAuth_WhenHeaderDoesNotStartWithBearer() throws Exception {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

        // Act
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain, times(1)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_ShouldSetAuthentication_WhenTokenIsValid() throws Exception {
        // Arrange
        String validToken = "Bearer valid.jwt.token";
        when(request.getHeader("Authorization")).thenReturn(validToken);
        when(jwtService.extractEmail("valid.jwt.token")).thenReturn("test@printpass.com");
        when(jwtService.isTokenValid("valid.jwt.token")).thenReturn(true);
        when(jwtService.extractRole("valid.jwt.token")).thenReturn("PRINT_SHOP");
        when(jwtService.extractUserId("valid.jwt.token")).thenReturn("123-uuid");

        // Act
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain, times(1)).doFilter(request, response);
        verify(request, times(1)).setAttribute("X-User-Id", "123-uuid");
        
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("test@printpass.com", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    @Test
    void doFilterInternal_ShouldNotSetAuth_WhenTokenIsInvalid() throws Exception {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid.token");
        when(jwtService.extractEmail(anyString())).thenReturn("test@printpass.com");
        when(jwtService.isTokenValid(anyString())).thenReturn(false);

        // Act
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain, times(1)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
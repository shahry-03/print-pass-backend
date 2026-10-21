package com.printpass.user.service;

import com.printpass.user.entity.User;
import com.printpass.user.enums.Role;
import com.printpass.user.exception.DuplicateResourceException;
import com.printpass.user.repository.PrintShopProfileRepository;
import com.printpass.user.repository.UserRepository;
import com.printpass.user.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PrintShopProfileRepository printShopProfileRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        // Setup fake data for testing
        mockUser = User.builder()
                .email("test@printpass.com")
                .password("hashed_password")
                .role(Role.PRINT_SHOP)
                .isActive(true)
                .emailVerified(true)
                .build();
    }

    @Test
    void createUser_ShouldSaveUser_WhenEmailDoesNotExist() {
        // 1. Arrange (Mock the dependencies)
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_password");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        // 2. Act (Call the actual method)
        User createdUser = userService.createUser("test@printpass.com", "plain_password", Role.PRINT_SHOP);

        // 3. Assert (Verify the results)
        assertNotNull(createdUser);
        assertEquals("test@printpass.com", createdUser.getEmail());
        
        // Verify that the repository methods were called exactly once
        verify(userRepository, times(1)).existsByEmail("test@printpass.com");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createUser_ShouldThrowException_WhenEmailAlreadyExists() {
        // 1. Arrange (Mock the dependency to return true)
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // 2. Act & Assert (Verify exception is thrown)
        DuplicateResourceException exception = assertThrows(DuplicateResourceException.class, () -> {
            userService.createUser("test@printpass.com", "plain_password", Role.PRINT_SHOP);
        });

        assertEquals("Email already exists! test@printpass.com", exception.getMessage());
        
        // Verify that save() was NEVER called
        verify(userRepository, never()).save(any(User.class));
    }
}
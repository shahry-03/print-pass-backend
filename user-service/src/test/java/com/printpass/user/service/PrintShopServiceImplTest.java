package com.printpass.user.service;

import com.printpass.user.dto.request.ShopRegisterRequest;
import com.printpass.user.dto.request.ShopUpdateRequest;
import com.printpass.user.dto.response.ShopResponse;
import com.printpass.user.entity.PrintShopProfile;
import com.printpass.user.entity.User;
import com.printpass.user.enums.Role;
import com.printpass.user.exception.ResourceNotFoundException;
import com.printpass.user.exception.UnauthorizedException;
import com.printpass.user.repository.PrintShopProfileRepository;
import com.printpass.user.service.impl.PrintShopServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrintShopServiceImplTest {

    @Mock
    private PrintShopProfileRepository printShopProfileRepository;

    @Mock
    private UserService userService;

    @Mock
    private OtpService otpService;

    @InjectMocks
    private PrintShopServiceImpl printShopService;

    private User mockUser;
    private PrintShopProfile mockShop;
    private UUID userId;
    private UUID shopId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        shopId = UUID.randomUUID();

        mockUser = User.builder()
                .id(userId)
                .email("shop@test.com")
                .role(Role.PRINT_SHOP)
                .isActive(true)
                .build();

        mockShop = PrintShopProfile.builder()
                .id(shopId)
                .user(mockUser)
                .shopName("Test Shop")
                .ownerName("Owner Name")
                .phone("03001234567")
                .address("Test Address")
                .city("Karachi")
                .isActive(true)
                .isVerified(false)
                .build();
    }

    // ==========================================
    // TEST 1: Register Shop (Success)
    // ==========================================
    @Test
    void registerShop_ShouldSaveShopAndSendOtp_WhenSuccessful() {
        // Arrange
        ShopRegisterRequest request = new ShopRegisterRequest();
        request.setEmail("shop@test.com");
        request.setPassword("Password123");
        request.setShopName("Test Shop");
        request.setOwnerName("Owner Name");
        request.setPhone("03001234567");
        request.setAddress("Test Address");
        request.setCity("Karachi");

        when(userService.createUser(anyString(), anyString(), any(Role.class))).thenReturn(mockUser);
        when(printShopProfileRepository.save(any(PrintShopProfile.class))).thenReturn(mockShop);

        // Act
        ShopResponse response = printShopService.registerShop(request);

        // Assert
        assertNotNull(response);
        assertEquals("Test Shop", response.getShopName());
        assertEquals("Karachi", response.getCity());
        assertFalse(response.isVerified());

        // Verify interactions
        verify(userService, times(1)).createUser(anyString(), anyString(), any(Role.class));
        verify(printShopProfileRepository, times(1)).save(any(PrintShopProfile.class));
        verify(otpService, times(1)).sendOtpAsync(mockUser.getEmail()); // OTP lazmi bhejna chahiye
    }

    // ==========================================
    // TEST 2: Update Shop (Success)
    // ==========================================
    @Test
    void updateShop_ShouldUpdateFields_WhenUserIsOwner() {
        // Arrange
        ShopUpdateRequest request = new ShopUpdateRequest();
        request.setShopName("Updated Shop");
        request.setOwnerName("Updated Owner");
        request.setPhone("03111234567");
        request.setAddress("New Address");
        request.setCity("Lahore");

        when(printShopProfileRepository.findById(shopId)).thenReturn(Optional.of(mockShop));
        when(printShopProfileRepository.save(any(PrintShopProfile.class))).thenReturn(mockShop);

        // Act
        ShopResponse response = printShopService.updateShop(shopId, userId, request);

        // Assert
        assertNotNull(response);
        // Verify that the entity was updated before save
        assertEquals("Updated Shop", mockShop.getShopName());
        assertEquals("Lahore", mockShop.getCity());
        verify(printShopProfileRepository, times(1)).save(mockShop);
    }

    // ==========================================
    // TEST 3: Update Shop (IDOR Protection Fail)
    // ==========================================
    @Test
    void updateShop_ShouldThrowUnauthorizedException_WhenUserIsNotOwner() {
        // Arrange
        UUID hackerId = UUID.randomUUID(); // Koi aur user ID
        ShopUpdateRequest request = new ShopUpdateRequest();

        when(printShopProfileRepository.findById(shopId)).thenReturn(Optional.of(mockShop));

        // Act & Assert
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            printShopService.updateShop(shopId, hackerId, request);
        });

        assertEquals("Unauthorized: You can only update your own shops!", exception.getMessage());
        // Verify that save was never called
        verify(printShopProfileRepository, never()).save(any(PrintShopProfile.class));
    }

    // ==========================================
    // TEST 4: Verify Shop (Admin Action)
    // ==========================================
    @Test
    void verifyShop_ShouldSetIsVerifiedToTrue() {
        // Arrange
        when(printShopProfileRepository.findById(shopId)).thenReturn(Optional.of(mockShop));

        // Act
        printShopService.verifyShop(shopId);

        // Assert
        assertTrue(mockShop.isVerified()); // Check if flag changed to true
        verify(printShopProfileRepository, times(1)).save(mockShop);
    }

    // ==========================================
    // TEST 5: Toggle Shop Status
    // ==========================================
    @Test
    void toggleShopStatus_ShouldToggleIsActive() {
        // Arrange
        when(printShopProfileRepository.findById(shopId)).thenReturn(Optional.of(mockShop));
        assertTrue(mockShop.isActive()); // Pehle check karein ke active hai

        // Act
        printShopService.toggleShopStatus(shopId);

        // Assert
        assertFalse(mockShop.isActive()); // Ab false ho jana chahiye
        verify(printShopProfileRepository, times(1)).save(mockShop);
    }

    // ==========================================
    // TEST 6: Not Found Exception
    // ==========================================
    @Test
    void verifyShop_ShouldThrowException_WhenShopDoesNotExist() {
        // Arrange
        when(printShopProfileRepository.findById(shopId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            printShopService.verifyShop(shopId);
        });

        assertTrue(exception.getMessage().contains("Shop not found with id: "));
    }
}
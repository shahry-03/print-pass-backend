package com.printpass.user.controller;

import com.printpass.user.dto.response.InternalUserResponse;
import com.printpass.user.dto.response.ShopVerificationResponse;
import com.printpass.user.entity.PrintShopProfile;
import com.printpass.user.exception.ResourceNotFoundException;
import com.printpass.user.repository.PrintShopProfileRepository;
import com.printpass.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/internal")
@RequiredArgsConstructor
public class InternalController {
    private final UserService userService;
    private final PrintShopProfileRepository printShopProfileRepository;

    //  Auth Service call karega — Login ke waqt
    @GetMapping("/users/by-email")
    public ResponseEntity<InternalUserResponse> getUserByEmail(@RequestParam String email) {
        //1. User ko email se database me dhundho
        //2. Agar user nahi mila toh Exception throw karo
        //3. Agar user mil gaya toh uska data InternalUserResponse me map karo aur return kar do
        // ye sare kam UserService ka method getUserByEmail kar raha he to hum wo use karenge
        InternalUserResponse internalUserResponse = userService.getUserByEmail(email);
        return ResponseEntity.ok(internalUserResponse);


    }

    @GetMapping("/shops/{shopId}/exists")
    public ResponseEntity<ShopVerificationResponse> verifyShop(
            @PathVariable UUID shopId) {

        PrintShopProfile shop = printShopProfileRepository
                .findById(shopId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Shop not found: " + shopId));

        return ResponseEntity.ok(
                ShopVerificationResponse.builder()
                        .isVerified(shop.isVerified())
                        .isActive(shop.isActive())
                        .build()
        );
    }

    @GetMapping("/users/{userId}/shop-verification")
    public ResponseEntity<ShopVerificationResponse> verifyShopByUserId(@PathVariable UUID userId) {

        PrintShopProfile shop = printShopProfileRepository.findByUser_Id(userId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No shop found for user: " + userId));

        return ResponseEntity.ok(ShopVerificationResponse.builder()
                .shopId(shop.getId()) // 🌟 PRO FIX: Asli Shop ID yahan set ki
                .isVerified(shop.isVerified())
                .isActive(shop.isActive())
                .build());
    }

}

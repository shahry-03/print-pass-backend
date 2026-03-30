package com.printpass.user.controller;

import com.printpass.user.dto.request.ShopRegisterRequest;
import com.printpass.user.dto.request.ShopUpdateRequest;
import com.printpass.user.dto.response.ShopResponse;
import com.printpass.user.service.PrintShopService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shops")
@RequiredArgsConstructor
public class ShopController {
    private final PrintShopService shopService;

    //  Public — Koi bhi register kar sakta hai
    @PostMapping("/register")
    public ResponseEntity<ShopResponse> registerShop(@Valid @RequestBody ShopRegisterRequest request) { // ShopRegisterRequest request body se aayega, jisme shop ka data hoga
        // ShopService ka registerShop method call karenge, jisme request pass karenge
        ShopResponse response = shopService.registerShop(request);
        // ShopResponse response aayega, jisme shop ka data hoga jo hum client ko denge
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    //  JWT Protected — Apni shops dekho
    @GetMapping("/my-shops")
    public ResponseEntity<List<ShopResponse>> getMyShops(
            @RequestAttribute("X-User-Id") UUID userId) {
        List<ShopResponse> shopResponse = shopService.getMyShops(userId);
        return ResponseEntity.ok(shopResponse);
    }

    //  JWT Protected — Shop update karo
    @PutMapping("/{shopId}")
    public ResponseEntity<ShopResponse> updateShop(
            @PathVariable UUID shopId,
            @RequestAttribute("X-User-Id") UUID userId,
            @Valid @RequestBody ShopUpdateRequest request) {
        ShopResponse response = shopService.updateShop(shopId, userId, request);
        return ResponseEntity.ok(response);

    }
}

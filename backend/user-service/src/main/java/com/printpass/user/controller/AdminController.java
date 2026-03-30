package com.printpass.user.controller;

import com.printpass.user.dto.response.ShopResponse;
import com.printpass.user.service.PrintShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final PrintShopService shopService;

    //  Saari shops dekho
    @GetMapping("/shops")
    public ResponseEntity<List<ShopResponse>> getAllShops(){
        List<ShopResponse> shops = shopService.getAllShops();
        return ResponseEntity.ok(shops);
    }

    //Shop Verify karo
    @PatchMapping("/shops/{shopId}/verify")
    public ResponseEntity<Void> verifyShop(@PathVariable UUID shopId){
        // Shop ID PathVariable  se aayega, jise hum shopService ke verifyShop method me pass karenge
        shopService.verifyShop(shopId);
        return ResponseEntity.noContent().build();
    }



    // Shop statusToggle karo
    @PatchMapping("/shops/{shopId}/toggle-status")
    public ResponseEntity<Void> toggleShopStatus(@PathVariable UUID shopId){
        // Shop ID PathVariable se aayega, jise hum shopService ke toggleShopStatus method me pass karenge
        shopService.toggleShopStatus(shopId);
        return ResponseEntity.noContent().build();
    }
}

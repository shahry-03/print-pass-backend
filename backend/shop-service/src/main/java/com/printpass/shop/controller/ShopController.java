package com.printpass.shop.controller;

import com.printpass.shop.dto.ShopRequest;
import com.printpass.shop.dto.ShopResponse;
import com.printpass.shop.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shops")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ShopResponse createShop(@RequestBody ShopRequest request) {
        return shopService.createShop(request);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ShopResponse> getAllShops() {
        return shopService.getAllShops();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ShopResponse getShopById(@PathVariable Long id) {
        return shopService.getShopById(id);
    }
}
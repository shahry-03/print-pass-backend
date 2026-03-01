package com.printpass.shop.service;

import com.printpass.shop.client.UserClient; // Naya import
import com.printpass.shop.dto.ShopRequest;
import com.printpass.shop.dto.ShopResponse;
import com.printpass.shop.dto.UserDto; // Naya import
import com.printpass.shop.entity.Shop;
import com.printpass.shop.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShopService {

    private final ShopRepository shopRepository;
    private final UserClient userClient; // Feign Client Inject kiya!

    public ShopResponse createShop(ShopRequest request) {

        // 1. Dukan save karne se pehle USER-SERVICE ko call lagayen
        UserDto vendor;
        try {
            vendor = userClient.getUserById(request.getVendorId());
        } catch (Exception e) {
            throw new RuntimeException("Error: Yeh Vendor ID mojood hi nahi hai!");
        }

        // 2. Check karein kya yeh waqai VENDOR hai?
        if (!"VENDOR".equals(vendor.getRole())) {
            throw new RuntimeException("Error: Sirf ek VENDOR hi dukan khol sakta hai. Yeh user STUDENT hai!");
        }

        // 3. Agar verification pass ho jaye, toh dukan save karein
        Shop shop = Shop.builder()
                .vendorId(request.getVendorId())
                .shopName(request.getShopName())
                .location(request.getLocation())
                .perPageCost(request.getPerPageCost())
                .isOpen(true)
                .build();

        Shop savedShop = shopRepository.save(shop);
        return mapToResponse(savedShop);
    }

    public ShopResponse getShopById(Long id) {
        Shop shop = shopRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Shop not Found!"));
        return mapToResponse(shop);
    }

    // Neechay getAllShops() aur mapToResponse() waise hi rahenge...
    public List<ShopResponse> getAllShops() {
        return shopRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    private ShopResponse mapToResponse(Shop shop) {
        return ShopResponse.builder()
                .id(shop.getId())
                .vendorId(shop.getVendorId())
                .shopName(shop.getShopName())
                .location(shop.getLocation())
                .perPageCost(shop.getPerPageCost())
                .isOpen(shop.isOpen())
                .build();
    }
}
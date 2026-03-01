package com.printpass.shop.service;

import com.printpass.shop.dto.ShopRequest;
import com.printpass.shop.dto.ShopResponse;
import com.printpass.shop.entity.Shop;
import com.printpass.shop.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShopService {

    private final ShopRepository shopRepository;

    public ShopResponse createShop(ShopRequest request) {
        Shop shop = Shop.builder()
                .vendorId(request.getVendorId())
                .shopName(request.getShopName())
                .location(request.getLocation())
                .perPageCost(request.getPerPageCost())
                .isOpen(true) // Jab dukan banegi toh default khuli hogi
                .build();

        Shop savedShop = shopRepository.save(shop);
        return mapToResponse(savedShop);
    }

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
package com.printpass.user.service;

import com.printpass.user.dto.request.ShopRegisterRequest;
import com.printpass.user.dto.request.ShopUpdateRequest;
import com.printpass.user.dto.response.ShopResponse;

import java.util.List;
import java.util.UUID;

public interface PrintShopService {
    // Vendor ke liye (Print Shop Owner)
    // 1. Nayi shop banaye ga aur DTO wapas dega
    ShopResponse registerShop(ShopRegisterRequest request);

    // 2. Ek user ki saari shops list mein wapas dega
    List<ShopResponse> getMyShops(UUID userId);

    // 3. Shop update karega aur updated Dto wapas dega
    ShopResponse updateShop(UUID shopId, UUID userId,  ShopUpdateRequest request);



    // Admin ke liye (Super User)
    // Saare shops nikalne ke liye
    List<ShopResponse> getAllShops();

    // Verify/Approve karne ke liye
    void verifyShop(UUID shopId);

    // 6. Sirf background mein status change karega, return kuch nahi karega
    void toggleShopStatus(UUID shopId);


}

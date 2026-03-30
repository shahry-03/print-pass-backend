package com.printpass.user.service.impl;

import com.printpass.user.dto.request.ShopRegisterRequest;
import com.printpass.user.dto.request.ShopUpdateRequest;
import com.printpass.user.dto.response.ShopResponse;
import com.printpass.user.entity.PrintShopProfile;
import com.printpass.user.entity.User;
import com.printpass.user.enums.Role;
import com.printpass.user.exception.ResourceNotFoundException;
import com.printpass.user.exception.UnauthorizedException;
import com.printpass.user.repository.PrintShopProfileRepository;
import com.printpass.user.service.PrintShopService;
import com.printpass.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.toList;


@RequiredArgsConstructor
@Service
public class PrintShopServiceImpl implements PrintShopService {
    private final PrintShopProfileRepository printShopProfileRepository;
    private final UserService userService;

    @Override
    @Transactional
    public ShopResponse registerShop(ShopRegisterRequest request) {

        //1. duplicate email check karenge kiyunki hum yaha user banane ke liye createUser method
        // use kar rahe he jo ki email duplicate check karta hai,
        // toh pehle se hi check kar lega aur exception throw karega
        //2. User banao userService call
        User newUser = userService.createUser(
                request.getEmail(),
                request.getPassword(),
                Role.PRINT_SHOP  // Role Strictly PrintShop assign kardiya
        );
        //3. Ab Shop (PrintShopProfile) ka object banayein aur naya User isme attach karein
        PrintShopProfile printShopProfile = PrintShopProfile.builder()
                .user(newUser)  // // Jo user upar bana, wo is dukaan ka malik (owner) ban gaya
                // ID aur CreatedAt Hibernate khud lagayega, humein manually nahi dena
                .shopName(request.getShopName())
                .ownerName(request.getOwnerName())
                .phone(request.getPhone())
                .address(request.getAddress())
                .city(request.getCity())
                .isActive(true)  // Nayi shop toh active hi hogi, admin block karega agar zarurat hui toh
                .isVerified(false)  // Naya shop toh verified nahi hoga, admin verify karega
                .build();

        //4.  Dukan ko database me save karo aur RETURNED object ko pakdo
        PrintShopProfile savedShop = printShopProfileRepository.save(printShopProfile);
        //5. ShopResponse return karo (savedShop se data nikal kar)
        return ShopResponse.builder()
                .id(savedShop.getId())
                .shopName(savedShop.getShopName())
                .ownerName(savedShop.getOwnerName())
                .phone(savedShop.getPhone())
                .address(savedShop.getAddress())
                .city(savedShop.getCity())
                .isVerified(savedShop.isVerified())
                .createdAt(savedShop.getCreatedAt())
                .isActive(savedShop.isActive())
                .build();

    }

    @Override
    public List<ShopResponse> getMyShops(UUID userId) {
        // 1. Database se is user ki saari shops (Entities) utha lo
        List<PrintShopProfile> printShopProfiles = printShopProfileRepository.findByUser_Id(userId);
        // 2. Stream aur Map ka jadoo
        List<ShopResponse> shopResponses = printShopProfiles.stream()
                .map(printShop -> ShopResponse.builder()   // 'printShop' belt par aane wali ek single entity hai
                        .id(printShop.getId())
                        .shopName(printShop.getShopName())
                        .ownerName(printShop.getOwnerName())
                        .phone(printShop.getPhone())
                        .address(printShop.getAddress())
                        .city(printShop.getCity())
                        .isVerified(printShop.isVerified())
                        .createdAt(printShop.getCreatedAt())
                        .isActive(printShop.isActive())
                        .build())
                .toList(); // Stream ke end me collect karlo List me
        // 3. Nayi list return kar do
        return shopResponses;
    }

    @Override
    @Transactional
    public ShopResponse updateShop(UUID shopId, UUID userId, ShopUpdateRequest request) {
        // 1. Pehle database se purani dukaan dhoondo (agar nahi mili toh error phenko)
        PrintShopProfile existingShop = printShopProfileRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found with id: " + shopId));

        // 🚨 SECURITY CHECK: IDOR Protection (Ownership Check) 🚨
        // Check karo ke database wali shop ka User ID, request bhejne wale User ID se match karta hai?
        if (!existingShop.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Unauthorized: You can only update your own shops!");
        }


        // 2. Naya data (jo frontend se form mein aaya hai) is purani dukaan mein daal do
        existingShop.setShopName(request.getShopName());
        existingShop.setOwnerName(request.getOwnerName());
        existingShop.setPhone(request.getPhone());
        existingShop.setAddress(request.getAddress());
        existingShop.setCity(request.getCity());
        // SECURITY NOTE: Humne yahan `isVerified` aur `createdAt` ko touch nahi kiya!
        // Wo wahi rahenge jo database mein pehle se save the.
        // 3. Updated dukaan ko dubara database me save kar do
        PrintShopProfile updatedShop =  printShopProfileRepository.save(existingShop);
        // 4. updated shop se Wapas ek naya DTO (ShopResponse) bana kar frontend ko bhej do
            return ShopResponse.builder()
                    .id(updatedShop.getId())
                    .shopName(updatedShop.getShopName())
                    .ownerName(updatedShop.getOwnerName())
                    .phone(updatedShop.getPhone())
                    .address(updatedShop.getAddress())
                    .city(updatedShop.getCity())
                    .isActive(updatedShop.isActive()) // Purana status hi wapas jayega
                    .isVerified(updatedShop.isVerified())   // Purana status hi wapas jayega
                    .createdAt(updatedShop.getCreatedAt())  // Purani date hi wapas jayegi
                    .build();

    }


    // Admin Methods
    @Override
    public List<ShopResponse> getAllShops() {

        List<PrintShopProfile> printShopProfiles = printShopProfileRepository.findAll();
        List<ShopResponse> shopResponses = printShopProfiles.stream()
                .map(printshop -> ShopResponse.builder()
                        .id(printshop.getId())
                        .shopName(printshop.getShopName())
                        .ownerName(printshop.getOwnerName())
                        .phone(printshop.getPhone())
                        .address(printshop.getAddress())
                        .city(printshop.getCity())
                        .isActive(printshop.isActive())
                        .isVerified(printshop.isVerified())
                        .createdAt(printshop.getCreatedAt())
                        .build())
                .toList();
        return shopResponses;

    }

    @Override
    @Transactional // Database ko safe rakhne ke liye aur dirty checking ke liye
    public void verifyShop(UUID shopId) {
        //1. Pehle Id se shop Dudhoondo
        PrintShopProfile shop = printShopProfileRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found with id: " + shopId));
        //2. Us shop ka status change kar do (isVerified = true)
        shop.setVerified(true);
        //3. Updated shop ko database me save kar do
        printShopProfileRepository.save(shop);

    }

    @Override
    @Transactional
    public void toggleShopStatus(UUID shopId) {
        PrintShopProfile printShop = printShopProfileRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found with id: " + shopId));
        // Ab dukaan ke malik ko nahi, sirf is makhsoos dukaan ko block/unblock karenge
        printShop.setActive(!printShop.isActive()); // Jo bhi current status hai, uska ulta kar do
        printShopProfileRepository.save(printShop);

    }
}

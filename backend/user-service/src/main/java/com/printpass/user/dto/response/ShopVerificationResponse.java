package com.printpass.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShopVerificationResponse {
    private UUID shopId; // 🌟 NEW: Asli Shop ID wapas bhejenge
    private boolean isVerified;
    private boolean isActive;  // ← Yeh bhi check karo!
}
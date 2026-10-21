package com.printpass.job.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopVerificationResponse {
    // User Service se aayega
    private UUID shopId;
    private boolean isVerified;
    private boolean isActive;
}
package com.printpass.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShopResponse {
    private UUID id;
    private String shopName;
    private String ownerName;
    private String phone;
    private String address;
    private String city;
    private boolean isVerified;
    private boolean isActive;  // new field add
    private LocalDateTime createdAt;

    // Password aur sensitive info kabhi response me nahi aayengi due to securtiy reasons

}

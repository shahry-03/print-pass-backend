package com.printpass.job.feign;

import com.printpass.job.dto.response.ShopVerificationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(
        name = "user-service",
        url = "${USER_SERVICE_URL:}", // YEH LINE ADD KAREIN
        path = "/api/v1/internal"
)
public interface UserServiceClient {
    
    @GetMapping("/users/{userId}/shop-verification")
    ShopVerificationResponse verifyShop(@PathVariable("userId") UUID userId);
}
package com.printpass.job.feign;

import com.printpass.job.dto.response.ShopVerificationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(
        name = "user-service",          // ← Eureka registered naam (Future proof)
        path = "/api/v1/internal"       // ← Base path (Khufiya darwaza)
)
public interface UserServiceClient {

    // ✅ Shop exist karti hai aur verified hai?
    // Print karne se pehle ye check lagana lazmi hai
    @GetMapping("/users/{userId}/shop-verification")
    ShopVerificationResponse verifyShop(@PathVariable("userId") UUID userId); // ← User ID se shop verify karenge, kyunki Job Service ke paas sirf userId hoga, shopId nahi
}
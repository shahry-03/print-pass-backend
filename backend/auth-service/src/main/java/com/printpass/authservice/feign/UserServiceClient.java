package com.printpass.authservice.feign;

import com.printpass.authservice.dto.response.InternalUserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "user-service",   // Yeh Eureka mein ja kar is naam ko dhoondega jo ke registered hoga
        path = "/api/v1/internal"  // Yeh us service ka andar wala khufiya rasta hai Base Path
)
public interface UserServiceClient {
    // Yeh bilkul aam Controller jaisa lag raha hai, lekin yeh asal mein
    // User Service ko HTTP GET request bhej raha hoga!
    // User Service ka Internal endpoint call karega
    // GET /api/v1/internal/users/by-email?email=...
    @GetMapping("/users/by-email")
    InternalUserResponse getUserByEmail(@RequestParam("email") String email);

}

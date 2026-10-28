package com.printpass.authservice.feign;

import com.printpass.authservice.dto.response.InternalUserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "user-service",
        url = "${USER_SERVICE_URL:}", // MAGIC LINE: Eureka bypass, Azure URL env var se aayega
        path = "/api/v1/internal"
)
public interface UserServiceClient {
    
    @GetMapping("/users/by-email")
    InternalUserResponse getUserByEmail(@RequestParam("email") String email);

}
package com.printpass.user.controller;

import com.printpass.user.dto.response.InternalUserResponse;
import com.printpass.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/internal")
@RequiredArgsConstructor
public class InternalController {
    private final UserService userService;

    //  Auth Service call karega — Login ke waqt
    @GetMapping("/users/by-email")
    public ResponseEntity<InternalUserResponse> getUserByEmail(@RequestParam String email) {
        //1. User ko email se database me dhundho
        //2. Agar user nahi mila toh Exception throw karo
        //3. Agar user mil gaya toh uska data InternalUserResponse me map karo aur return kar do
        // ye sare kam UserService ka method getUserByEmail kar raha he to hum wo use karenge
        InternalUserResponse internalUserResponse = userService.getUserByEmail(email);
        return ResponseEntity.ok(internalUserResponse);


    }

}

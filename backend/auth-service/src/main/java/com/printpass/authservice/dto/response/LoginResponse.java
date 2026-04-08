package com.printpass.authservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;  // jwt Token
    private String role;  // role: PRINT_SHOP  or ADMIN
    private String email;  // kis ne login kia uska email
    private Long expiresIn;  // token kitni der tak valid hoga
}

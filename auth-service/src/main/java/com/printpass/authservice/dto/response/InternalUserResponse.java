package com.printpass.authservice.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InternalUserResponse {

    // ye data aayega user service se
    private UUID id;
    private String email;
    private String role;  // PRINT_SHOP or ADMIN  String use kar rahe hain taake enum ka masla na aaye
    private String password; // Yeh HASHED password aayega database se! BCrypt se compare karenge
    @JsonProperty("isActive")
    private boolean isActive;   // Taake hum check kar sakein ke user block toh nahi hai

}

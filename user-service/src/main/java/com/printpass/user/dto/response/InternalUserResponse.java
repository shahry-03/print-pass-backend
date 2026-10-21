package com.printpass.user.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.printpass.user.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor      // JSON se object banane ke liye laazmi hai
@AllArgsConstructor     // @Builder ko theek se chalane ke liye laazmi hai
public class InternalUserResponse {
    // Sirf Auth Service use Karega
    private UUID id;
    private String email;
    private String password;  // ← Hashed password — Auth Service verify karega
    private Role role;
    @JsonProperty("isActive")
    private boolean isActive;

}

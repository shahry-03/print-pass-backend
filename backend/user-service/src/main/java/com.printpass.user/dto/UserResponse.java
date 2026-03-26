package main.java.com.printpass.dto;

import main.java.com.printpass.enums.Role;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private Role role;
    private BigDecimal walletBalance;
    // Note: Isme password ka field nahi hai! 🛡️
}
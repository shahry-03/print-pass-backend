package main.java.com.printpass.dto;

import main.java.com.printpass.enums.Role;
import lombok.Data;

@Data
public class UserRequest {
    private String name;
    private String email;
    private String password;
    private Role role;
}
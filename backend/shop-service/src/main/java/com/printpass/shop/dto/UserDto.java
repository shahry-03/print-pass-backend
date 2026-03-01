package com.printpass.shop.dto;

import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private String name;
    private String role; // Humein check karna hai ke yeh VENDOR hai ya nahi
}
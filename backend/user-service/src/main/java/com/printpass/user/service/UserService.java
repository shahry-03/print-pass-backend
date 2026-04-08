package com.printpass.user.service;

import com.printpass.user.dto.response.InternalUserResponse;
import com.printpass.user.entity.User;
import com.printpass.user.enums.Role;

import java.util.UUID;

public interface UserService {

    User createUser(String email, String password, Role role);

    // Auth Service ke liye — Email se user dhundho
    InternalUserResponse getUserByEmail(String email);

    // Admin — User active/inactive karo
    void toggleUserStatus(UUID userId);

    boolean isShopVerified(UUID shopId);
}

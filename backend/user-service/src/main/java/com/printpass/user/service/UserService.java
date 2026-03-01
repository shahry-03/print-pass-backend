package com.printpass.user.service;

import com.printpass.user.dto.UserRequest;
import com.printpass.user.dto.UserResponse;
import com.printpass.user.entity.User;
import com.printpass.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // <--- Hashing Tool Inject kiya

    public UserResponse createUser(UserRequest request) {
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // <--- Password Encrypt ho raha hai!
                .role(request.getRole())
                .walletBalance(BigDecimal.ZERO)
                .build();

        User savedUser = userRepository.save(user);
        return mapToUserResponse(savedUser);
    }

    // Database se saare users la kar unhein Response DTO mein convert karna
    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream().map(this::mapToUserResponse).toList();
    }

    //method: get user by its id Ek
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found!"));
        return mapToUserResponse(user);
    }

    // Ek chota sa helper method jo User Entity ko UserResponse mein badalta hai
    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .walletBalance(user.getWalletBalance())
                .build();
    }
}
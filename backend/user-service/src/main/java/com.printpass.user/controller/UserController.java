package com.printpass.user.controller;

import main.java.com.printpass.dto.AuthRequest;
import main.java.com.printpass.dto.UserRequest;
import main.java.com.printpass.dto.UserResponse;
import main.java.com.printpass.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@RequestBody UserRequest request) {
        return userService.createUser(request);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers();
    }

    // A new endpoint that fetches a user using their ID.
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UserResponse getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    // 🔄 Naya aur Behtar Login (React ki khushi ke liye)
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody AuthRequest request) {

        // 1. Password verify karein aur JWT Token hasil karein
        String token = userService.login(request);

        // 2. Jadu: Us User ki details dhoondein jo login ho raha hai
        UserResponse loggedInUser = userService.getAllUsers().stream()
                .filter(u -> u.getEmail().equals(request.getEmail()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 3. Data ko bilkul us tarah Pack karein jaisa React (Copilot) chahta hai!
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", loggedInUser);

        return ResponseEntity.ok(response);
    }
}
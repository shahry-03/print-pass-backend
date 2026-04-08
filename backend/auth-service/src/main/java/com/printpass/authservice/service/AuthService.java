package com.printpass.authservice.service;

import com.printpass.authservice.dto.request.LoginRequest;
import com.printpass.authservice.dto.response.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest loginRequest);
}

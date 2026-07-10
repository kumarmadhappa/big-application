package com.bigapplication.userservice.service;

import com.bigapplication.userservice.dto.request.CreateUserRequest;
import com.bigapplication.userservice.dto.request.LoginRequest;
import com.bigapplication.userservice.dto.request.RefreshTokenRequest;
import com.bigapplication.userservice.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(CreateUserRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refresh(RefreshTokenRequest request);
}

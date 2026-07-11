package com.bigapplication.bankingsystem.service;

import com.bigapplication.bankingsystem.dto.request.LoginRequest;
import com.bigapplication.bankingsystem.dto.request.RefreshTokenRequest;
import com.bigapplication.bankingsystem.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);

    AuthResponse refresh(RefreshTokenRequest request);
}

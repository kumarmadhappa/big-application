package com.bigapplication.userservice.controller;

import com.bigapplication.userservice.dto.request.CreateUserRequest;
import com.bigapplication.userservice.dto.request.LoginRequest;
import com.bigapplication.userservice.dto.request.RefreshTokenRequest;
import com.bigapplication.userservice.dto.response.ApiResponse;
import com.bigapplication.userservice.dto.response.AuthResponse;
import com.bigapplication.userservice.service.AuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody CreateUserRequest request) {
        log.info("Registration request received for username={}", request.getUsername());
        return ResponseEntity.ok(ApiResponse.<AuthResponse>builder()
                .success(true)
                .message("User registered successfully")
                .data(authService.register(request))
                .build());
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received for login={}", request.getLogin());
        return ResponseEntity.ok(ApiResponse.<AuthResponse>builder()
                .success(true)
                .message("Login successful")
                .data(authService.login(request))
                .build());
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Token refresh request received");
        return ResponseEntity.ok(ApiResponse.<AuthResponse>builder()
                .success(true)
                .message("Token refreshed successfully")
                .data(authService.refresh(request))
                .build());
    }
}

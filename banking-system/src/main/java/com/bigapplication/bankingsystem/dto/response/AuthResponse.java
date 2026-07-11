package com.bigapplication.bankingsystem.dto.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AuthResponse {
    String tokenType;
    String accessToken;
    String refreshToken;
    long expiresInSeconds;
    AuthUserResponse user;
}

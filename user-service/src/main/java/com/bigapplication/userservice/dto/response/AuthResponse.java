package com.bigapplication.userservice.dto.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AuthResponse {
    String tokenType;
    String accessToken;
    String refreshToken;
    long expiresInSeconds;
    UserResponse user;
}

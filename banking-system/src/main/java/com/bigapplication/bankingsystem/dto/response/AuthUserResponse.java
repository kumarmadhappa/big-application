package com.bigapplication.bankingsystem.dto.response;

import com.bigapplication.bankingsystem.entity.UserRole;
import java.util.Set;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AuthUserResponse {
    Long id;
    String username;
    String email;
    Set<UserRole> roles;
}

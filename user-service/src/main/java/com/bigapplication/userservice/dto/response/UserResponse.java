package com.bigapplication.userservice.dto.response;

import com.bigapplication.userservice.entity.Role;
import java.time.Instant;
import java.util.Set;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserResponse {
    Long id;
    String username;
    String email;
    String firstName;
    String lastName;
    boolean enabled;
    Set<Role> roles;
    Instant createdAt;
    Instant updatedAt;
}

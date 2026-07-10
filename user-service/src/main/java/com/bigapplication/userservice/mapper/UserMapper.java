package com.bigapplication.userservice.mapper;

import com.bigapplication.userservice.dto.request.CreateUserRequest;
import com.bigapplication.userservice.dto.request.UpdateUserRequest;
import com.bigapplication.userservice.dto.response.UserResponse;
import com.bigapplication.userservice.entity.Role;
import com.bigapplication.userservice.entity.User;
import java.util.HashSet;
import java.util.Set;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(CreateUserRequest request, PasswordEncoder passwordEncoder) {
        Set<Role> roles = request.getRoles() == null || request.getRoles().isEmpty()
                ? Set.of(Role.USER)
                : request.getRoles();

        return User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .enabled(true)
                .roles(new HashSet<>(roles))
                .build();
    }

    public void applyUpdate(User user, UpdateUserRequest request, PasswordEncoder passwordEncoder) {
        if (request.getUsername() != null) {
            user.setUsername(request.getUsername());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            user.setRoles(new HashSet<>(request.getRoles()));
        }
    }

    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .enabled(user.isEnabled())
                .roles(user.getRoles())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}

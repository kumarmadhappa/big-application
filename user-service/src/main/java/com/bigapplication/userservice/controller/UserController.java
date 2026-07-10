package com.bigapplication.userservice.controller;

import com.bigapplication.userservice.dto.request.CreateUserRequest;
import com.bigapplication.userservice.dto.request.UpdateUserRequest;
import com.bigapplication.userservice.dto.response.ApiResponse;
import com.bigapplication.userservice.dto.response.UserResponse;
import com.bigapplication.userservice.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> create(@Valid @RequestBody CreateUserRequest request) {
        log.info("Create user request received for username={}", request.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<UserResponse>builder()
                .success(true)
                .message("User created successfully")
                .data(userService.createUser(request))
                .build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> update(@PathVariable Long id,
                                                            @Valid @RequestBody UpdateUserRequest request) {
        log.info("Update user request received for id={}", id);
        return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                .success(true)
                .message("User updated successfully")
                .data(userService.updateUser(id, request))
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getById(@PathVariable Long id) {
        log.info("Get user request received for id={}", id);
        return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                .success(true)
                .message("User fetched successfully")
                .data(userService.getUser(id))
                .build());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAll() {
        log.info("Get all users request received");
        return ResponseEntity.ok(ApiResponse.<List<UserResponse>>builder()
                .success(true)
                .message("Users fetched successfully")
                .data(userService.getAllUsers())
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        log.info("Delete user request received for id={}", id);
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("User deleted successfully")
                .data(null)
                .build());
    }
}

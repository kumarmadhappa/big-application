package com.bigapplication.userservice.service;

import com.bigapplication.userservice.dto.request.CreateUserRequest;
import com.bigapplication.userservice.dto.request.UpdateUserRequest;
import com.bigapplication.userservice.dto.response.UserResponse;
import java.util.List;

public interface UserService {

    UserResponse createUser(CreateUserRequest request);

    UserResponse updateUser(Long id, UpdateUserRequest request);

    UserResponse getUser(Long id);

    List<UserResponse> getAllUsers();

    void deleteUser(Long id);
}

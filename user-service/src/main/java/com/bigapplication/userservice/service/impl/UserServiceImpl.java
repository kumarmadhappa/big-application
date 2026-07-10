package com.bigapplication.userservice.service.impl;

import com.bigapplication.userservice.dto.request.CreateUserRequest;
import com.bigapplication.userservice.dto.request.UpdateUserRequest;
import com.bigapplication.userservice.dto.response.UserResponse;
import com.bigapplication.userservice.entity.Role;
import com.bigapplication.userservice.entity.User;
import com.bigapplication.userservice.exception.DuplicateResourceException;
import com.bigapplication.userservice.exception.ResourceNotFoundException;
import com.bigapplication.userservice.mapper.UserMapper;
import com.bigapplication.userservice.repository.UserRepository;
import com.bigapplication.userservice.service.UserService;
import java.util.HashSet;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserResponse createUser(CreateUserRequest request) {
        log.info("Creating user username={} email={}", request.getUsername(), request.getEmail());
        ensureUnique(request.getUsername(), request.getEmail(), null);
        User user = userMapper.toEntity(request, passwordEncoder);
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            user.setRoles(new HashSet<>(List.of(Role.USER)));
        }
        User savedUser = userRepository.save(user);
        log.info("User created with id={}", savedUser.getId());
        return userMapper.toResponse(savedUser);
    }

    @Override
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        log.info("Updating user id={}", id);
        User user = findUser(id);
        ensureUnique(request.getUsername(), request.getEmail(), id);
        userMapper.applyUpdate(user, request, passwordEncoder);
        User updatedUser = userRepository.save(user);
        log.info("User updated id={}", id);
        return userMapper.toResponse(updatedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUser(Long id) {
        log.debug("Fetching user id={}", id);
        return userMapper.toResponse(findUser(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        log.debug("Fetching all users");
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    public void deleteUser(Long id) {
        log.info("Deleting user id={}", id);
        userRepository.delete(findUser(id));
        log.info("User deleted id={}", id);
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    private void ensureUnique(String username, String email, Long currentId) {
        if (username != null && userRepository.findByUsername(username)
                .filter(user -> !user.getId().equals(currentId))
                .isPresent()) {
            log.warn("Username conflict detected for username={}", username);
            throw new DuplicateResourceException("Username already exists: " + username);
        }
        if (email != null && userRepository.findByEmail(email)
                .filter(user -> !user.getId().equals(currentId))
                .isPresent()) {
            log.warn("Email conflict detected for email={}", email);
            throw new DuplicateResourceException("Email already exists: " + email);
        }
    }
}

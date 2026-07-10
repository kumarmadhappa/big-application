package com.bigapplication.userservice.service.impl;

import com.bigapplication.userservice.dto.request.CreateUserRequest;
import com.bigapplication.userservice.dto.request.LoginRequest;
import com.bigapplication.userservice.dto.request.RefreshTokenRequest;
import com.bigapplication.userservice.dto.response.AuthResponse;
import com.bigapplication.userservice.entity.User;
import com.bigapplication.userservice.exception.InvalidCredentialsException;
import com.bigapplication.userservice.repository.UserRepository;
import com.bigapplication.userservice.security.JwtService;
import com.bigapplication.userservice.security.UserPrincipal;
import com.bigapplication.userservice.service.AuthService;
import com.bigapplication.userservice.service.UserService;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(UserService userService,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public AuthResponse register(CreateUserRequest request) {
        log.info("Registering user username={}", request.getUsername());
        Long userId = userService.createUser(request).getId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidCredentialsException("User registration failed"));
        log.info("User registration completed for id={}", userId);
        return buildAuthResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("Authenticating login={}", request.getLogin());
        User user = userRepository.findByUsernameOrEmail(request.getLogin(), request.getLogin())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username/email or password"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Authentication failed for login={}", request.getLogin());
            throw new InvalidCredentialsException("Invalid username/email or password");
        }
        log.info("Authentication succeeded for username={}", user.getUsername());
        return buildAuthResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse refresh(RefreshTokenRequest request) {
        try {
            String username = jwtService.extractSubject(request.getRefreshToken());
            if (!jwtService.isTokenType(request.getRefreshToken(), "refresh")) {
                log.warn("Refresh token rejected because token type is not refresh");
                throw new InvalidCredentialsException("Invalid refresh token");
            }
            User user = userRepository.findByUsernameOrEmail(username, username)
                    .orElseThrow(() -> new InvalidCredentialsException("Invalid refresh token"));
            if (!jwtService.isTokenValid(request.getRefreshToken(), user.getUsername())) {
                log.warn("Refresh token validation failed for username={}", username);
                throw new InvalidCredentialsException("Invalid refresh token");
            }
            log.info("Token refresh succeeded for username={}", username);
            return buildAuthResponse(user);
        } catch (JwtException ex) {
            log.warn("Refresh token parsing failed", ex);
            throw new InvalidCredentialsException("Invalid refresh token");
        }
    }

    private AuthResponse buildAuthResponse(User user) {
        UserPrincipal principal = UserPrincipal.from(user);
        return AuthResponse.builder()
                .tokenType("Bearer")
                .accessToken(jwtService.generateAccessToken(principal))
                .refreshToken(jwtService.generateRefreshToken(principal))
                .expiresInSeconds(jwtService.getAccessTokenExpirationSeconds())
                .user(jwtService.toUserResponse(user))
                .build();
    }
}

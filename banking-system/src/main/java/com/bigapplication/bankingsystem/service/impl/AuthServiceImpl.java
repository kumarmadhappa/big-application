package com.bigapplication.bankingsystem.service.impl;

import com.bigapplication.bankingsystem.dto.request.LoginRequest;
import com.bigapplication.bankingsystem.dto.request.RefreshTokenRequest;
import com.bigapplication.bankingsystem.dto.response.AuthResponse;
import com.bigapplication.bankingsystem.entity.BankUser;
import com.bigapplication.bankingsystem.exception.InvalidCredentialsException;
import com.bigapplication.bankingsystem.repository.BankUserRepository;
import com.bigapplication.bankingsystem.security.JwtService;
import com.bigapplication.bankingsystem.security.UserPrincipal;
import com.bigapplication.bankingsystem.service.AuthService;
import io.jsonwebtoken.JwtException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final BankUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(BankUserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        BankUser user = userRepository.findByUsernameOrEmail(request.getLogin(), request.getLogin())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username/email or password"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid username/email or password");
        }
        return buildAuthResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse refresh(RefreshTokenRequest request) {
        try {
            String username = jwtService.extractSubject(request.getRefreshToken());
            if (!jwtService.isTokenType(request.getRefreshToken(), "refresh")) {
                throw new InvalidCredentialsException("Invalid refresh token");
            }
            BankUser user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new InvalidCredentialsException("Invalid refresh token"));
            if (!jwtService.isTokenValid(request.getRefreshToken(), user.getUsername())) {
                throw new InvalidCredentialsException("Invalid refresh token");
            }
            return buildAuthResponse(user);
        } catch (JwtException ex) {
            throw new InvalidCredentialsException("Invalid refresh token");
        }
    }

    private AuthResponse buildAuthResponse(BankUser user) {
        UserPrincipal principal = UserPrincipal.from(user);
        return AuthResponse.builder()
                .tokenType("Bearer")
                .accessToken(jwtService.generateAccessToken(principal))
                .refreshToken(jwtService.generateRefreshToken(principal))
                .expiresInSeconds(jwtService.getAccessTokenExpirationSeconds())
                .user(jwtService.toAuthUserResponse(user))
                .build();
    }
}

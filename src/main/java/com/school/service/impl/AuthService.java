package com.school.service.impl;

import com.school.dto.request.*;
import com.school.dto.response.AuthResponse;
import com.school.dto.response.UserResponse;
import com.school.entity.AuthToken;
import com.school.entity.PasswordResetToken;
import com.school.entity.User;
import com.school.exception.BadRequestException;
import com.school.exception.ResourceNotFoundException;
import com.school.exception.UnauthorizedException;
import com.school.repository.AuthTokenRepository;
import com.school.repository.PasswordResetTokenRepository;
import com.school.repository.UserRepository;
import com.school.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AuthTokenRepository authTokenRepository;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid credentials");
        }
        if (!user.getIsActive()) {
            throw new UnauthorizedException("Account is inactive");
        }

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        String accessToken = tokenProvider.generateAccessToken(user.getEmail(), user.getRole().name(), user.getId().toString());
        String refreshToken = tokenProvider.generateRefreshToken(user.getEmail());

        authTokenRepository.save(AuthToken.builder()
                .user(user)
                .refreshToken(refreshToken)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .user(mapUserToResponse(user))
                .build();
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        AuthToken authToken = authTokenRepository.findByRefreshToken(request.getRefreshToken())
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (authToken.getRevokedAt() != null || authToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UnauthorizedException("Refresh token expired or revoked");
        }

        User user = authToken.getUser();
        String newAccessToken = tokenProvider.generateAccessToken(user.getEmail(), user.getRole().name(), user.getId().toString());
        String newRefreshToken = tokenProvider.generateRefreshToken(user.getEmail());

        authToken.setRevokedAt(LocalDateTime.now());
        authTokenRepository.save(authToken);

        authTokenRepository.save(AuthToken.builder()
                .user(user)
                .refreshToken(newRefreshToken)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .user(mapUserToResponse(user))
                .build();
    }

    @Transactional
    public void logout(String refreshToken) {
        authTokenRepository.findByRefreshToken(refreshToken).ifPresent(token -> {
            token.setRevokedAt(LocalDateTime.now());
            authTokenRepository.save(token);
        });
    }

    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));

        String token = String.format("%06d", new Random().nextInt(999999));
        resetTokenRepository.save(PasswordResetToken.builder()
                .user(user)
                .token(token)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .build());
        // In production: send token via email
    }

    @Transactional
    public void resetPassword(UUID userId, ResetPasswordRequest request) {
        PasswordResetToken resetToken = resetTokenRepository
                .findByUserIdAndTokenAndUsedAtIsNull(userId, request.getToken())
                .orElseThrow(() -> new BadRequestException("Invalid or expired token"));

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Token has expired");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsedAt(LocalDateTime.now());
        resetTokenRepository.save(resetToken);
    }

    private UserResponse mapUserToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .isActive(user.getIsActive())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .build();
    }
}

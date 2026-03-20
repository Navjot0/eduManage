package com.school.service.impl;

import com.school.dto.request.CreateUserRequest;
import com.school.dto.request.UpdateUserRequest;
import com.school.dto.response.UserResponse;
import com.school.entity.User;
import com.school.enums.UserRole;
import com.school.exception.ConflictException;
import com.school.exception.ResourceNotFoundException;
import com.school.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UserResponse> getAllUsers(Boolean isActive) {
        return userRepository.findAll().stream()
                .filter(u -> isActive == null || u.getIsActive().equals(isActive))
                .map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<UserResponse> getUsersByRole(UserRole role, Boolean isActive) {
        return userRepository.findByRole(role).stream()
                .filter(u -> isActive == null || u.getIsActive().equals(isActive))
                .map(this::mapToResponse).collect(Collectors.toList());
    }

    public UserResponse getUserById(UUID id) {
        return mapToResponse(findUserById(id));
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already registered: " + request.getEmail());
        }
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .phone(request.getPhone())
                .avatarUrl(request.getAvatarUrl())
                .isActive(true)
                .build();
        return mapToResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse updateUser(UUID id, UpdateUserRequest request) {
        User user = findUserById(id);
        if (request.getName() != null) user.setName(request.getName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getAvatarUrl() != null) user.setAvatarUrl(request.getAvatarUrl());
        return mapToResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse toggleUserStatus(UUID id) {
        User user = findUserById(id);
        user.setIsActive(!user.getIsActive());
        User saved = userRepository.save(user);
        return mapToResponse(saved);
    }

    @Transactional
    public UserResponse setUserStatus(UUID id, boolean active) {
        User user = findUserById(id);
        user.setIsActive(active);
        User saved = userRepository.save(user);
        return mapToResponse(saved);
    }

    @Transactional
    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) throw new ResourceNotFoundException("User", "id", id);
        userRepository.deleteById(id);
    }

    public User findUserById(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    public UserResponse mapToResponse(User user) {
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
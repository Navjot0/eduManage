package com.school.controller;

import com.school.dto.response.ApiResponse;
import com.school.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * UTILITY CONTROLLER — Fix BCrypt password hashes in the database.
 *
 * Step 1: GET  /dev/hash?password=password123          → preview the BCrypt hash
 * Step 2: POST /dev/fix-passwords?password=password123 → update ALL users in DB
 *
 * IMPORTANT: Remove or comment out this class before going to production.
 */
@RestController
@RequestMapping("/dev")
@RequiredArgsConstructor
@Slf4j
public class DevToolsController {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    /** Returns the BCrypt hash for any plain-text password. */
    @GetMapping("/hash")
    public ApiResponse<Map<String, String>> generateHash(@RequestParam String password) {
        String hash = passwordEncoder.encode(password);
        Map<String, String> result = new HashMap<>();
        result.put("password", password);
        result.put("bcryptHash", hash);
        result.put("hashLength", String.valueOf(hash.length()));
        result.put("verified", String.valueOf(passwordEncoder.matches(password, hash)));
        return ApiResponse.success(result);
    }

    /** Updates every user's password_hash in the DB to the BCrypt hash of the given password. */
    @PostMapping("/fix-passwords")
    public ApiResponse<Map<String, Object>> fixAllPasswords(@RequestParam(defaultValue = "password123") String password) {
        String hash = passwordEncoder.encode(password);
        log.warn("DEV: Updating ALL user passwords to BCrypt hash of '{}'", password);

        int[] count = {0};
        userRepository.findAll().forEach(user -> {
            user.setPasswordHash(hash);
            userRepository.save(user);
            count[0]++;
        });

        Map<String, Object> result = new HashMap<>();
        result.put("usersUpdated", count[0]);
        result.put("password", password);
        result.put("hashUsed", hash);
        result.put("message", "All users now have correct BCrypt password. Login with: " + password);
        return ApiResponse.success("Passwords fixed", result);
    }
}
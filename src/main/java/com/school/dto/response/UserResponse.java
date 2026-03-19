package com.school.dto.response;
import com.school.enums.UserRole;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder
public class UserResponse {
    private UUID id;
    private String name;
    private String email;
    private UserRole role;
    private String phone;
    private String avatarUrl;
    private Boolean isActive;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
}

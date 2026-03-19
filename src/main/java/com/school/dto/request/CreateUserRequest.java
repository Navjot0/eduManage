package com.school.dto.request;
import com.school.enums.UserRole;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateUserRequest {
    @NotBlank @Size(max = 100) private String name;
    @Email @NotBlank private String email;
    @NotBlank @Size(min = 6) private String password;
    @NotNull private UserRole role;
    private String phone;
    private String avatarUrl;
}

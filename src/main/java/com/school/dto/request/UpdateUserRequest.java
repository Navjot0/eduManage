package com.school.dto.request;
import lombok.Data;

@Data
public class UpdateUserRequest {
    private String name;
    private String phone;
    private String avatarUrl;
}

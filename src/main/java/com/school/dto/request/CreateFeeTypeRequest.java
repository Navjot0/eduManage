package com.school.dto.request;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateFeeTypeRequest {
    @NotBlank private String name;
    private String description;
}

package com.school.dto.request;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.UUID;

@Data
public class CreateClassRequest {
    @NotBlank private String className;
    @NotBlank private String section;
    @NotBlank private String academicYear;
    private UUID classTeacherId;
}

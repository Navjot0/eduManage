package com.school.dto.request;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class CreateStudentRequest {
    @NotBlank private String name;
    @Email @NotBlank private String email;
    @NotBlank @Size(min = 6) private String password;

    /**
     * Roll number within the class. Optional — if omitted, the next available
     * number in the class is auto-assigned (001, 002, 003...).
     * Must be unique within the same className + section.
     */
    private String rollNumber;

    @NotBlank private String className;
    @NotBlank private String section;
    private String parentName;
    private String parentPhone;
    private String address;
    private LocalDate dateOfBirth;
    private LocalDate admissionDate;
    private String phone;
}
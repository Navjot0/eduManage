package com.school.dto.request;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class CreateStudentRequest {
    @NotBlank private String name;
    @Email @NotBlank private String email;
    @NotBlank @Size(min = 6) private String password;
    @NotBlank private String rollNumber;
    @NotBlank private String className;
    @NotBlank private String section;
    private String parentName;
    private String parentPhone;
    private String address;
    private LocalDate dateOfBirth;
    private LocalDate admissionDate;
    private String phone;
}

package com.school.dto.request;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class CreateTeacherRequest {
    @NotBlank private String name;
    @Email @NotBlank private String email;
    @NotBlank @Size(min = 6) private String password;
    @NotBlank private String employeeId;
    @NotBlank private String subject;
    private String department;
    private String qualification;
    private LocalDate joiningDate;
    private String phone;
}

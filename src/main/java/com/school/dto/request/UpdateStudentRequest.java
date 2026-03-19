package com.school.dto.request;
import com.school.enums.StatusActive;
import lombok.Data;
import java.time.LocalDate;

@Data
public class UpdateStudentRequest {
    private String className;
    private String section;
    private String parentName;
    private String parentPhone;
    private String address;
    private LocalDate dateOfBirth;
    private StatusActive status;
}

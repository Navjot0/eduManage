package com.school.dto.response;
import com.school.enums.StatusActive;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder
public class StudentResponse {
    private UUID id;
    private UUID userId;
    private String name;
    private String email;
    private String rollNumber;
    private String className;
    private String section;
    private String academicYear;
    private String parentName;
    private String parentPhone;
    private String address;
    private LocalDate dateOfBirth;
    private LocalDate admissionDate;
    private StatusActive status;

    // Linked class info (from classes table)
    private UUID classId;
    private String classTeacherName;
    private Integer totalStudentsInClass;

    private LocalDateTime createdAt;
}
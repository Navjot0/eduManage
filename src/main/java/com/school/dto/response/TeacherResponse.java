package com.school.dto.response;
import com.school.enums.StatusActive;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder
public class TeacherResponse {
    private UUID id;
    private UUID userId;
    private String name;
    private String email;
    private String employeeId;
    private String subject;
    private String department;
    private String qualification;
    private LocalDate joiningDate;
    private StatusActive status;
    private LocalDateTime createdAt;
}

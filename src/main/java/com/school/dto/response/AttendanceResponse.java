package com.school.dto.response;
import com.school.enums.AttendanceStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder
public class AttendanceResponse {
    private UUID id;
    private UUID studentId;
    private String studentName;
    private String rollNumber;
    private UUID teacherId;
    private String className;
    private String section;
    private String subject;
    private LocalDate date;
    private AttendanceStatus status;
    private String remarks;
    private LocalDateTime markedAt;
}

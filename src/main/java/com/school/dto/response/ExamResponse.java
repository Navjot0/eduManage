package com.school.dto.response;
import com.school.enums.ExamStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder
public class ExamResponse {
    private UUID id;
    private String name;
    private String className;
    private String section;
    private String academicYear;
    private LocalDate startDate;
    private LocalDate endDate;
    private ExamStatus status;
    private LocalDateTime createdAt;
}

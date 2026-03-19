package com.school.dto.response;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder
public class ExamResultResponse {
    private UUID id;
    private UUID examId;
    private String examName;
    private UUID studentId;
    private String studentName;
    private String subject;
    private Integer maxMarks;
    private Integer obtainedMarks;
    private String grade;
    private Double percentage;
    private String remarks;
    private LocalDate examDate;
    private LocalDateTime createdAt;
}

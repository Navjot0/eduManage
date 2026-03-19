package com.school.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class CreateExamResultRequest {
    @NotNull private UUID studentId;
    @NotBlank private String subject;
    @NotNull private Integer maxMarks;
    @NotNull private Integer obtainedMarks;
    @NotBlank private String grade;
    private String remarks;
    @NotNull private LocalDate examDate;
}

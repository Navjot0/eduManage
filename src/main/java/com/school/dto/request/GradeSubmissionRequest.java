package com.school.dto.request;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GradeSubmissionRequest {
    @NotNull private Integer obtainedMarks;
    private String feedback;
}

package com.school.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CreateAssignmentRequest {
    @NotBlank private String title;
    @NotBlank private String subject;
    @NotBlank private String description;
    @NotBlank private String className;
    @NotBlank private String section;
    @NotNull private LocalDateTime dueDate;
    private Integer maxMarks;
    private String fileUrl;
}

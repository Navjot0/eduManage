package com.school.dto.response;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder
public class AssignmentResponse {
    private UUID id;
    private String title;
    private String subject;
    private String description;
    private String className;
    private String section;
    private UUID teacherId;
    private String teacherName;
    private LocalDateTime dueDate;
    private Integer maxMarks;
    private String fileUrl;
    private Boolean isActive;
    private LocalDateTime createdAt;
}

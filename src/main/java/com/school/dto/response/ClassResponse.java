package com.school.dto.response;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder
public class ClassResponse {
    private UUID id;
    private String className;
    private String section;
    private String academicYear;
    private UUID classTeacherId;
    private String classTeacherName;
    private Integer studentCount;
    private LocalDateTime createdAt;
}

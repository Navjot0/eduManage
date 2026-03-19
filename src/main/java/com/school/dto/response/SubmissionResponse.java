package com.school.dto.response;
import com.school.enums.AssignmentStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder
public class SubmissionResponse {
    private UUID id;
    private UUID assignmentId;
    private String assignmentTitle;
    private UUID studentId;
    private String studentName;
    private AssignmentStatus status;
    private LocalDateTime submittedAt;
    private String fileUrl;
    private Integer obtainedMarks;
    private Integer maxMarks;
    private String feedback;
    private LocalDateTime gradedAt;
}

package com.school.dto.request;
import lombok.Data;
import java.util.UUID;

@Data
public class SubmitAssignmentRequest {
    private UUID studentId;
    private String fileUrl;
}

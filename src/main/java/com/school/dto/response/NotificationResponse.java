package com.school.dto.response;
import com.school.enums.NotificationType;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder
public class NotificationResponse {
    private UUID id;
    private String title;
    private String message;
    private NotificationType type;
    private Boolean isRead;
    private String referenceType;
    private UUID referenceId;
    private LocalDateTime createdAt;
}

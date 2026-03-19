package com.school.dto.request;
import com.school.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class CreateNotificationRequest {
    @NotNull private UUID userId;
    @NotBlank private String title;
    @NotBlank private String message;
    private NotificationType type = NotificationType.info;
    private String referenceType;
    private UUID referenceId;
}

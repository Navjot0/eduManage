package com.school.dto.response;
import com.school.enums.AnnouncementTarget;
import com.school.enums.PriorityLevel;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder
public class AnnouncementResponse {
    private UUID id;
    private String title;
    private String content;
    private UUID authorId;
    private String authorName;
    private AnnouncementTarget targetRole;
    private PriorityLevel priority;
    private Boolean isActive;
    private long readCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

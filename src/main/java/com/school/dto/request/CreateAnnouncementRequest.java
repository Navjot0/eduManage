package com.school.dto.request;
import com.school.enums.AnnouncementTarget;
import com.school.enums.PriorityLevel;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateAnnouncementRequest {
    @NotBlank private String title;
    @NotBlank private String content;
    private AnnouncementTarget targetRole = AnnouncementTarget.all;
    private PriorityLevel priority = PriorityLevel.medium;
}

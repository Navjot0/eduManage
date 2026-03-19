package com.school.dto.response;
import com.school.enums.MaterialType;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder
public class StudyMaterialResponse {
    private UUID id;
    private String title;
    private String subject;
    private String description;
    private MaterialType fileType;
    private String fileUrl;
    private Integer fileSizeKb;
    private String className;
    private String section;
    private UUID uploadedById;
    private String uploadedByName;
    private String academicYear;
    private Boolean isActive;
    private LocalDateTime uploadedAt;
}

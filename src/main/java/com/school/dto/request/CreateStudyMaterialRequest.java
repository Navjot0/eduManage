package com.school.dto.request;
import com.school.enums.MaterialType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateStudyMaterialRequest {
    @NotBlank private String title;
    @NotBlank private String subject;
    private String description;
    @NotNull private MaterialType fileType;
    @NotBlank private String fileUrl;
    private Integer fileSizeKb;
    @NotBlank private String className;
    private String section;
    @NotBlank private String academicYear;
}

package com.school.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class CreateExamRequest {
    @NotBlank private String name;
    @NotBlank private String className;
    private String section;
    @NotBlank private String academicYear;
    @NotNull private LocalDate startDate;
    @NotNull private LocalDate endDate;
}

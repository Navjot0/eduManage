package com.school.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class BulkAttendanceRequest {
    @NotBlank private String className;
    @NotBlank private String section;
    private String subject;
    @NotNull private LocalDate date;
    @NotEmpty private List<AttendanceRequest> records;
}

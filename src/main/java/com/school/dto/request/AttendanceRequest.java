package com.school.dto.request;
import com.school.enums.AttendanceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class AttendanceRequest {
    @NotNull private UUID studentId;
    @NotNull private AttendanceStatus status;
    private String subject;
    private String remarks;
    private LocalDate date;
}

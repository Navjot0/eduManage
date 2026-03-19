package com.school.dto.request;
import com.school.enums.WeekdayEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalTime;
import java.util.UUID;

@Data
public class CreateTimetableSlotRequest {
    @NotBlank private String className;
    @NotBlank private String section;
    @NotNull private WeekdayEnum day;
    @NotNull private LocalTime startTime;
    @NotNull private LocalTime endTime;
    @NotBlank private String subject;
    @NotNull private UUID teacherId;
    private String room;
    @NotBlank private String academicYear;
}

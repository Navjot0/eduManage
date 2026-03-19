package com.school.dto.response;
import com.school.enums.WeekdayEnum;
import lombok.Builder;
import lombok.Data;
import java.time.LocalTime;
import java.util.UUID;

@Data @Builder
public class TimetableSlotResponse {
    private UUID id;
    private String className;
    private String section;
    private WeekdayEnum day;
    private LocalTime startTime;
    private LocalTime endTime;
    private String subject;
    private UUID teacherId;
    private String teacherName;
    private String room;
    private String academicYear;
    private Boolean isActive;
}

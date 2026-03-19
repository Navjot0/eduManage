package com.school.dto.response;
import lombok.Builder;
import lombok.Data;

@Data @Builder
public class AttendanceSummaryResponse {
    private long totalDays;
    private long presentDays;
    private long absentDays;
    private long lateDays;
    private double attendancePercentage;
}

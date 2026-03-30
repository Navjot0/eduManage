package com.school.dto.response;

import com.school.enums.AttendanceStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ClassAttendanceResponse {

    private String className;
    private String section;
    private String subject;
    private LocalDate date;

    // Summary counts for the day / range
    private int totalStudents;
    private int presentCount;
    private int absentCount;
    private int lateCount;
    private int notMarkedCount;
    private double attendancePercent;

    // Per-student breakdown
    private List<StudentAttendanceEntry> students;

    @Data
    @Builder
    public static class StudentAttendanceEntry {
        private UUID studentId;
        private String studentName;
        private String rollNumber;
        private AttendanceStatus status;   // null = not marked
        private String remarks;
        private LocalDate date;
    }
}
package com.school.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ClassAttendanceSummaryResponse {

    private String className;
    private String section;
    private LocalDate from;
    private LocalDate to;
    private int totalStudents;

    // Per-student summary for the date range
    private List<StudentSummaryEntry> students;

    // Daily breakdown (each date → present/absent/late counts)
    private List<DailySummaryEntry> dailySummary;

    // Class-level totals
    private long totalRecords;
    private long totalPresent;
    private long totalAbsent;
    private long totalLate;
    private double classAttendancePercent;

    @Data
    @Builder
    public static class StudentSummaryEntry {
        private UUID studentId;
        private String studentName;
        private String rollNumber;
        private long presentDays;
        private long absentDays;
        private long lateDays;
        private long totalMarkedDays;
        private double attendancePercent;
    }

    @Data
    @Builder
    public static class DailySummaryEntry {
        private LocalDate date;
        private int presentCount;
        private int absentCount;
        private int lateCount;
        private int totalMarked;
        private double attendancePercent;
    }
}
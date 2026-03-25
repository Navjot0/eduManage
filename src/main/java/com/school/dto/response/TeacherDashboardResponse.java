package com.school.dto.response;

import com.school.enums.WeekdayEnum;
import lombok.Builder;
import lombok.Data;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class TeacherDashboardResponse {

    private UUID teacherId;
    private String teacherName;
    private String employeeId;
    private String subject;
    private String department;

    /** Classes where this teacher is the assigned class teacher */
    private List<AssignedClassInfo> classTeacherOf;

    /** Distinct classes this teacher teaches (from timetable) */
    private List<TaughtClassInfo> teachingClasses;

    /** Summary counts */
    private int totalClassesAsClassTeacher;
    private int totalClassesTeaching;
    private int totalStudentsInCharge;

    // ── Inner types ──────────────────────────────────────────────────────

    @Data
    @Builder
    public static class AssignedClassInfo {
        private UUID classId;
        private String className;
        private String section;
        private String academicYear;
        private int studentCount;
        private String role;   // "Class Teacher"
    }

    @Data
    @Builder
    public static class TaughtClassInfo {
        private String className;
        private String section;
        private String subject;
        private String academicYear;
        private int studentCount;
        private List<TimetableEntry> schedule;
    }

    @Data
    @Builder
    public static class TimetableEntry {
        private WeekdayEnum day;
        private LocalTime startTime;
        private LocalTime endTime;
        private String room;
    }
}

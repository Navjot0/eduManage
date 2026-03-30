package com.school.controller;

import com.school.dto.request.AttendanceRequest;
import com.school.dto.request.BulkAttendanceRequest;
import com.school.dto.response.ApiResponse;
import com.school.dto.response.AttendanceResponse;
import com.school.dto.response.ClassAttendanceResponse;
import com.school.dto.response.ClassAttendanceSummaryResponse;
import com.school.dto.response.AttendanceSummaryResponse;
import com.school.enums.AttendanceStatus;
import com.school.security.UserPrincipal;
import com.school.service.impl.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/attendance")
@RequiredArgsConstructor
@Tag(name = "Attendance", description = "Attendance management APIs")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @GetMapping("/student/{studentId}")
    @Operation(summary = "Get attendance records for a student")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> getStudentAttendance(
            @PathVariable UUID studentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.getStudentAttendance(studentId, from, to)));
    }

    @GetMapping("/student/{studentId}/summary")
    @Operation(summary = "Get attendance summary/percentage for a student")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    public ResponseEntity<ApiResponse<AttendanceSummaryResponse>> getAttendanceSummary(
            @PathVariable UUID studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.getStudentAttendanceSummary(studentId, from, to)));
    }

    @GetMapping("/class")
    @Operation(summary = "Get attendance for an entire class on a specific date")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> getClassAttendance(
            @RequestParam String className,
            @RequestParam String section,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.getClassAttendanceByDate(className, section, date)));
    }

    // ── Admin-only: class-level attendance ───────────────────────────────────

    @GetMapping("/classes/{className}/{section}")
    @Operation(
            summary = "Admin: get full attendance for a class on a specific date",
            description = "Returns all students in the class with their attendance status. " +
                    "Students not marked show status=null. Filter by subject optionally.")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ClassAttendanceResponse>> getClassAttendanceForDate(
            @PathVariable String className,
            @PathVariable String section,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(
                    iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String subject) {
        ClassAttendanceResponse response = attendanceService
                .getClassAttendanceForDate(className, section, subject, date);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/classes/{className}/{section}/summary")
    @Operation(
            summary = "Admin: attendance summary for a class over a date range",
            description = "Returns per-student stats (present/absent/late/%) and daily breakdown. " +
                    "Useful for report cards and parent meetings.")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ClassAttendanceSummaryResponse>> getClassAttendanceSummary(
            @PathVariable String className,
            @PathVariable String section,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(
                    iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(
                    iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate to) {
        ClassAttendanceSummaryResponse response = attendanceService
                .getClassAttendanceSummary(className, section, from, to);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/classes/{className}/{section}/range")
    @Operation(
            summary = "Admin: raw attendance records for a class over a date range",
            description = "Returns all individual attendance records sorted by date then roll number. " +
                    "Filter by subject optionally.")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<java.util.List<AttendanceResponse>>> getClassAttendanceRange(
            @PathVariable String className,
            @PathVariable String section,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(
                    iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(
                    iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String subject) {
        return ResponseEntity.ok(ApiResponse.success(
                attendanceService.getClassAttendanceRange(className, section, from, to, subject)));
    }

    @PostMapping
    @Operation(summary = "Mark attendance for a single student")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<ApiResponse<AttendanceResponse>> markAttendance(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AttendanceRequest request) {
        // Resolve teacher ID from logged-in user
        UUID teacherId = resolveTeacherId(principal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Attendance marked", attendanceService.markAttendance(teacherId, request)));
    }

    @PostMapping("/bulk")
    @Operation(summary = "Mark attendance for entire class at once")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> markBulkAttendance(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody BulkAttendanceRequest request) {
        UUID teacherId = resolveTeacherId(principal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Bulk attendance marked", attendanceService.markBulkAttendance(teacherId, request)));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update an attendance record")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<ApiResponse<AttendanceResponse>> updateAttendance(
            @PathVariable UUID id,
            @RequestParam AttendanceStatus status,
            @RequestParam(required = false) String remarks) {
        return ResponseEntity.ok(ApiResponse.success("Attendance updated", attendanceService.updateAttendance(id, status, remarks)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an attendance record")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteAttendance(@PathVariable UUID id) {
        attendanceService.deleteAttendance(id);
        return ResponseEntity.ok(ApiResponse.success("Attendance record deleted", null));
    }

    // Resolve teacher UUID from principal — in production, inject TeacherRepository or use a helper
    private UUID resolveTeacherId(UserPrincipal principal) {
        // For teacher role, userId is used as a lookup key through TeacherRepository.findByUserId
        // The actual resolution is delegated to the service using the userId stored in the principal
        return UUID.fromString(principal.getUserId());
    }
}
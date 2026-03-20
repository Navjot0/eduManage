package com.school.controller;

import com.school.dto.request.CreateTeacherRequest;
import com.school.dto.request.UpdateTeacherRequest;
import com.school.dto.response.ApiResponse;
import com.school.dto.response.TeacherResponse;
import com.school.enums.StatusActive;
import com.school.security.UserPrincipal;
import com.school.service.impl.TeacherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/teachers")
@RequiredArgsConstructor
@Tag(name = "Teachers", description = "Teacher management APIs")
public class TeacherController {

    private final TeacherService teacherService;

    @GetMapping
    @Operation(summary = "Get all teachers. Filter by department and/or status (active/inactive)")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<ApiResponse<List<TeacherResponse>>> getTeachers(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) StatusActive status) {
        List<TeacherResponse> teachers = department != null
                ? teacherService.getTeachersByDepartment(department, status)
                : teacherService.getAllTeachers(status);
        return ResponseEntity.ok(ApiResponse.success(teachers));
    }

    @GetMapping("/me")
    @Operation(summary = "Get logged-in teacher's own profile (uses JWT userId)")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<ApiResponse<TeacherResponse>> getMyProfile(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                teacherService.getTeacherByUserId(UUID.fromString(principal.getUserId()))));
    }

    @GetMapping("/by-user/{userId}")
    @Operation(summary = "Get teacher by their User ID")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<ApiResponse<TeacherResponse>> getByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(ApiResponse.success(teacherService.getTeacherByUserId(userId)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get teacher by teacher ID (not user ID)")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<ApiResponse<TeacherResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(teacherService.getTeacherById(id)));
    }

    @PostMapping
    @Operation(summary = "Add a new teacher")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TeacherResponse>> createTeacher(@Valid @RequestBody CreateTeacherRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Teacher added", teacherService.createTeacher(request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update teacher details")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TeacherResponse>> updateTeacher(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTeacherRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Teacher updated", teacherService.updateTeacher(id, request)));
    }

    @PatchMapping("/{id}/toggle-status")
    @Operation(summary = "Toggle teacher active/inactive status — also updates user account")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TeacherResponse>> toggleTeacherStatus(@PathVariable UUID id) {
        TeacherResponse teacher = teacherService.toggleTeacherStatus(id);
        String msg = teacher.getStatus() == StatusActive.active
                ? "Teacher activated successfully" : "Teacher deactivated successfully";
        return ResponseEntity.ok(ApiResponse.success(msg, teacher));
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate a teacher — enables login")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TeacherResponse>> activateTeacher(@PathVariable UUID id) {
        TeacherResponse teacher = teacherService.setTeacherStatus(id, StatusActive.active);
        return ResponseEntity.ok(ApiResponse.success("Teacher activated successfully", teacher));
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate a teacher — disables login")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TeacherResponse>> deactivateTeacher(@PathVariable UUID id) {
        TeacherResponse teacher = teacherService.setTeacherStatus(id, StatusActive.inactive);
        return ResponseEntity.ok(ApiResponse.success("Teacher deactivated successfully", teacher));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove a teacher")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteTeacher(@PathVariable UUID id) {
        teacherService.deleteTeacher(id);
        return ResponseEntity.ok(ApiResponse.success("Teacher removed", null));
    }
}
package com.school.controller;

import com.school.dto.request.CreateTeacherRequest;
import com.school.dto.request.UpdateTeacherRequest;
import com.school.dto.response.ApiResponse;
import com.school.dto.response.TeacherResponse;
import com.school.service.impl.TeacherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @Operation(summary = "Get all teachers, optionally filtered by department")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<ApiResponse<List<TeacherResponse>>> getTeachers(
            @RequestParam(required = false) String department) {
        List<TeacherResponse> teachers = department != null
                ? teacherService.getTeachersByDepartment(department)
                : teacherService.getAllTeachers();
        return ResponseEntity.ok(ApiResponse.success(teachers));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get teacher by ID")
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

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove a teacher")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteTeacher(@PathVariable UUID id) {
        teacherService.deleteTeacher(id);
        return ResponseEntity.ok(ApiResponse.success("Teacher removed", null));
    }
}

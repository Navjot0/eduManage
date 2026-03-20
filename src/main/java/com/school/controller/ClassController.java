package com.school.controller;

import com.school.dto.request.CreateClassRequest;
import com.school.dto.response.ApiResponse;
import com.school.dto.response.ClassResponse;
import com.school.service.impl.ClassService;
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
@RequestMapping("/classes")
@RequiredArgsConstructor
@Tag(name = "Classes", description = "Class management APIs")
public class ClassController {

    private final ClassService classService;

    @GetMapping
    @Operation(summary = "Get all classes, optionally filtered by academic year")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<ApiResponse<List<ClassResponse>>> getClasses(
            @RequestParam(required = false) String academicYear) {
        List<ClassResponse> classes = academicYear != null
                ? classService.getClassesByAcademicYear(academicYear)
                : classService.getAllClasses();
        return ResponseEntity.ok(ApiResponse.success(classes));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get class by ID")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<ApiResponse<ClassResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(classService.getClassById(id)));
    }

    @PostMapping
    @Operation(summary = "Create a new class")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ClassResponse>> createClass(@Valid @RequestBody CreateClassRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Class created", classService.createClass(request)));
    }

    @PatchMapping("/{id}/assign-teacher/{teacherId}")
    @Operation(summary = "Assign a class teacher")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ClassResponse>> assignTeacher(
            @PathVariable UUID id, @PathVariable UUID teacherId) {
        return ResponseEntity.ok(ApiResponse.success("Teacher assigned", classService.assignTeacher(id, teacherId)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a class")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteClass(@PathVariable UUID id) {
        classService.deleteClass(id);
        return ResponseEntity.ok(ApiResponse.success("Class deleted", null));
    }

    @PostMapping("/{id}/sync-count")
    @Operation(summary = "Re-sync student_count from actual students table (fixes data drift)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ClassResponse>> syncStudentCount(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Count synced", classService.syncStudentCount(id)));
    }
}
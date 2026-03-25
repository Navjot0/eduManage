package com.school.controller;

import com.school.dto.request.CreateClassRequest;
import com.school.dto.response.ApiResponse;
import com.school.dto.response.ClassResponse;
import com.school.dto.response.StudentResponse;
import com.school.enums.StatusActive;
import com.school.service.impl.StudentService;
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
    private final StudentService studentService;

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

    @GetMapping("/{id}/students")
    @Operation(summary = "Get all students in a class (by class ID)")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<ApiResponse<java.util.List<StudentResponse>>> getStudentsByClassId(
            @PathVariable java.util.UUID id,
            @RequestParam(required = false) StatusActive status) {
        ClassResponse cls = classService.getClassById(id);
        java.util.List<StudentResponse> students = studentService.getStudentsByClass(
                cls.getClassName(), cls.getSection(), status);
        return ResponseEntity.ok(ApiResponse.success(students));
    }

    @GetMapping("/by-name/{className}/{section}/students")
    @Operation(summary = "Get students by class name + section directly")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<ApiResponse<java.util.List<StudentResponse>>> getStudentsByClassName(
            @PathVariable String className,
            @PathVariable String section,
            @RequestParam(required = false) StatusActive status) {
        java.util.List<StudentResponse> students = studentService.getStudentsByClass(
                className, section, status);
        return ResponseEntity.ok(ApiResponse.success(students));
    }

    @GetMapping("/by-name/{className}/{section}")
    @Operation(summary = "Get class details by class name + section (returns all matching academic years)")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<ApiResponse<java.util.List<ClassResponse>>> getByClassName(
            @PathVariable String className,
            @PathVariable String section) {
        java.util.List<ClassResponse> classes = classService.getClassesByNameAndSection(className, section);
        return ResponseEntity.ok(ApiResponse.success(classes));
    }

    @PostMapping("/{id}/sync-count")
    @Operation(summary = "Re-sync student_count from actual students table (fixes data drift)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ClassResponse>> syncStudentCount(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Count synced", classService.syncStudentCount(id)));
    }
}
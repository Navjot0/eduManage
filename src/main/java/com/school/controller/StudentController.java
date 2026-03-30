package com.school.controller;

import com.school.dto.request.CreateStudentRequest;
import com.school.dto.request.UpdateStudentRequest;
import com.school.dto.response.ApiResponse;
import com.school.dto.response.StudentResponse;
import com.school.enums.StatusActive;
import com.school.security.UserPrincipal;
import com.school.service.impl.StudentService;
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
@RequestMapping("/students")
@RequiredArgsConstructor
@Tag(name = "Students", description = "Student management APIs")
public class StudentController {

    private final StudentService studentService;

    @GetMapping
    @Operation(summary = "Get all students. Filter by class/section and/or status (active/inactive)")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<ApiResponse<List<StudentResponse>>> getStudents(
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String section,
            @RequestParam(required = false) StatusActive status) {
        List<StudentResponse> students = (className != null && section != null)
                ? studentService.getStudentsByClass(className, section, status)
                : studentService.getAllStudents(status);
        return ResponseEntity.ok(ApiResponse.success(students));
    }

    @GetMapping("/me")
    @Operation(summary = "Get logged-in student's own profile (uses JWT userId)")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    public ResponseEntity<ApiResponse<StudentResponse>> getMyProfile(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                studentService.getStudentByUserId(UUID.fromString(principal.getUserId()))));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get student by student ID (not user ID)")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    public ResponseEntity<ApiResponse<StudentResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(studentService.getStudentById(id)));
    }

    @GetMapping("/roll/{rollNumber}")
    @Operation(summary = "Get student by roll number (global — use /roll/{rollNumber}/class instead)")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<ApiResponse<StudentResponse>> getByRollNumber(
            @PathVariable String rollNumber) {
        return ResponseEntity.ok(ApiResponse.success(
                studentService.getStudentByRollNumber(rollNumber)));
    }

    @GetMapping("/roll/{rollNumber}/class")
    @Operation(summary = "Get student by roll number scoped to a class (preferred)")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<ApiResponse<StudentResponse>> getByRollNumberAndClass(
            @PathVariable String rollNumber,
            @RequestParam String className,
            @RequestParam String section) {
        return ResponseEntity.ok(ApiResponse.success(
                studentService.getStudentByRollNumberAndClass(rollNumber, className, section)));
    }

    @PostMapping
    @Operation(summary = "Enroll a new student")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<StudentResponse>> createStudent(@Valid @RequestBody CreateStudentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Student enrolled", studentService.createStudent(request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update student details")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<ApiResponse<StudentResponse>> updateStudent(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateStudentRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Student updated", studentService.updateStudent(id, request)));
    }

    @PatchMapping("/{id}/toggle-status")
    @Operation(summary = "Toggle student active/inactive status — also updates user account and class count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<StudentResponse>> toggleStudentStatus(@PathVariable UUID id) {
        StudentResponse student = studentService.toggleStudentStatus(id);
        String msg = student.getStatus() == StatusActive.active
                ? "Student activated successfully" : "Student deactivated successfully";
        return ResponseEntity.ok(ApiResponse.success(msg, student));
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate a student — enables login and increments class count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<StudentResponse>> activateStudent(@PathVariable UUID id) {
        StudentResponse student = studentService.setStudentStatus(id, StatusActive.active);
        return ResponseEntity.ok(ApiResponse.success("Student activated successfully", student));
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate a student — disables login and decrements class count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<StudentResponse>> deactivateStudent(@PathVariable UUID id) {
        StudentResponse student = studentService.setStudentStatus(id, StatusActive.inactive);
        return ResponseEntity.ok(ApiResponse.success("Student deactivated successfully", student));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a student record")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteStudent(@PathVariable UUID id) {
        studentService.deleteStudent(id);
        return ResponseEntity.ok(ApiResponse.success("Student deleted", null));
    }
}
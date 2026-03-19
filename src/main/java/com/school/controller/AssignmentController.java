package com.school.controller;

import com.school.dto.request.CreateAssignmentRequest;
import com.school.dto.request.GradeSubmissionRequest;
import com.school.dto.request.SubmitAssignmentRequest;
import com.school.dto.response.ApiResponse;
import com.school.dto.response.AssignmentResponse;
import com.school.dto.response.SubmissionResponse;
import com.school.security.UserPrincipal;
import com.school.service.impl.AssignmentService;
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
@RequestMapping("/assignments")
@RequiredArgsConstructor
@Tag(name = "Assignments", description = "Assignment and submission management APIs")
public class AssignmentController {

    private final AssignmentService assignmentService;

    @GetMapping
    @Operation(summary = "Get assignments by class and section")
    public ResponseEntity<ApiResponse<List<AssignmentResponse>>> getByClass(
            @RequestParam String className, @RequestParam String section) {
        return ResponseEntity.ok(ApiResponse.success(assignmentService.getAssignmentsByClass(className, section)));
    }

    @GetMapping("/teacher/{teacherId}")
    @Operation(summary = "Get all assignments created by a teacher")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<ApiResponse<List<AssignmentResponse>>> getByTeacher(@PathVariable UUID teacherId) {
        return ResponseEntity.ok(ApiResponse.success(assignmentService.getAssignmentsByTeacher(teacherId)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get assignment by ID")
    public ResponseEntity<ApiResponse<AssignmentResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(assignmentService.getAssignmentById(id)));
    }

    @PostMapping
    @Operation(summary = "Create a new assignment")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<ApiResponse<AssignmentResponse>> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateAssignmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Assignment created",
                        assignmentService.createAssignment(UUID.fromString(principal.getUserId()), request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an assignment")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<ApiResponse<AssignmentResponse>> update(
            @PathVariable UUID id, @Valid @RequestBody CreateAssignmentRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Assignment updated", assignmentService.updateAssignment(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an assignment")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        assignmentService.deleteAssignment(id);
        return ResponseEntity.ok(ApiResponse.success("Assignment deleted", null));
    }

    // Submissions
    @GetMapping("/{assignmentId}/submissions")
    @Operation(summary = "Get all submissions for an assignment")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<ApiResponse<List<SubmissionResponse>>> getSubmissions(@PathVariable UUID assignmentId) {
        return ResponseEntity.ok(ApiResponse.success(assignmentService.getSubmissionsForAssignment(assignmentId)));
    }

    @GetMapping("/submissions/student/{studentId}")
    @Operation(summary = "Get all submissions by a student")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    public ResponseEntity<ApiResponse<List<SubmissionResponse>>> getByStudent(@PathVariable UUID studentId) {
        return ResponseEntity.ok(ApiResponse.success(assignmentService.getSubmissionsByStudent(studentId)));
    }

    @PostMapping("/{assignmentId}/submit")
    @Operation(summary = "Submit an assignment (student)")
    @PreAuthorize("hasAnyRole('STUDENT','ADMIN')")
    public ResponseEntity<ApiResponse<SubmissionResponse>> submit(
            @PathVariable UUID assignmentId,
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody SubmitAssignmentRequest request) {
        UUID studentId = request.getStudentId() != null
                ? request.getStudentId() : UUID.fromString(principal.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Assignment submitted",
                        assignmentService.submitAssignment(assignmentId, studentId, request)));
    }

    @PatchMapping("/submissions/{submissionId}/grade")
    @Operation(summary = "Grade a submission")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<ApiResponse<SubmissionResponse>> grade(
            @PathVariable UUID submissionId,
            @Valid @RequestBody GradeSubmissionRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Submission graded",
                assignmentService.gradeSubmission(submissionId, request)));
    }
}

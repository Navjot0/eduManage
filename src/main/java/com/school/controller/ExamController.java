package com.school.controller;

import com.school.dto.request.CreateExamRequest;
import com.school.dto.request.CreateExamResultRequest;
import com.school.dto.response.ApiResponse;
import com.school.dto.response.ExamResponse;
import com.school.dto.response.ExamResultResponse;
import com.school.enums.ExamStatus;
import com.school.security.UserPrincipal;
import com.school.service.impl.ExamService;
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
@RequestMapping("/exams")
@RequiredArgsConstructor
@Tag(name = "Exams", description = "Exam and results management APIs")
public class ExamController {

    private final ExamService examService;

    @GetMapping
    @Operation(summary = "Get all exams, optionally filter by class and academic year")
    public ResponseEntity<ApiResponse<List<ExamResponse>>> getExams(
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String academicYear) {
        List<ExamResponse> exams = (className != null && academicYear != null)
                ? examService.getExamsByClass(className, academicYear)
                : examService.getAllExams();
        return ResponseEntity.ok(ApiResponse.success(exams));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get exam by ID")
    public ResponseEntity<ApiResponse<ExamResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(examService.getExamById(id)));
    }

    @PostMapping
    @Operation(summary = "Create a new exam")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<ApiResponse<ExamResponse>> createExam(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateExamRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Exam created",
                        examService.createExam(UUID.fromString(principal.getUserId()), request)));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update exam status (upcoming/ongoing/completed)")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<ApiResponse<ExamResponse>> updateStatus(
            @PathVariable UUID id, @RequestParam ExamStatus status) {
        return ResponseEntity.ok(ApiResponse.success("Exam status updated", examService.updateExamStatus(id, status)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an exam")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteExam(@PathVariable UUID id) {
        examService.deleteExam(id);
        return ResponseEntity.ok(ApiResponse.success("Exam deleted", null));
    }

    // Results
    @GetMapping("/{examId}/results")
    @Operation(summary = "Get all results for an exam")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<ApiResponse<List<ExamResultResponse>>> getExamResults(@PathVariable UUID examId) {
        return ResponseEntity.ok(ApiResponse.success(examService.getResultsByExam(examId)));
    }

    @GetMapping("/{examId}/results/student/{studentId}")
    @Operation(summary = "Get a student's results for a specific exam")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    public ResponseEntity<ApiResponse<List<ExamResultResponse>>> getStudentResults(
            @PathVariable UUID examId, @PathVariable UUID studentId) {
        return ResponseEntity.ok(ApiResponse.success(examService.getStudentResultsByExam(examId, studentId)));
    }

    @GetMapping("/results/student/{studentId}")
    @Operation(summary = "Get all exam results for a student across all exams")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    public ResponseEntity<ApiResponse<List<ExamResultResponse>>> getAllStudentResults(@PathVariable UUID studentId) {
        return ResponseEntity.ok(ApiResponse.success(examService.getResultsByStudent(studentId)));
    }

    @PostMapping("/{examId}/results")
    @Operation(summary = "Add exam result for a student")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<ApiResponse<ExamResultResponse>> addResult(
            @PathVariable UUID examId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateExamResultRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Result added",
                        examService.addExamResult(examId, UUID.fromString(principal.getUserId()), request)));
    }

    @PutMapping("/results/{resultId}")
    @Operation(summary = "Update an exam result")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<ApiResponse<ExamResultResponse>> updateResult(
            @PathVariable UUID resultId,
            @Valid @RequestBody CreateExamResultRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Result updated", examService.updateExamResult(resultId, request)));
    }
}

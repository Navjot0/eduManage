package com.school.controller;

import com.school.dto.request.CreateFeeRecordRequest;
import com.school.dto.request.CreateFeeTypeRequest;
import com.school.dto.request.UpdateFeeRecordRequest;
import com.school.dto.response.ApiResponse;
import com.school.dto.response.FeeRecordResponse;
import com.school.dto.response.FeeTypeResponse;
import com.school.enums.FeeStatus;
import com.school.service.impl.FeeService;
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
@RequiredArgsConstructor
@Tag(name = "Fees", description = "Fee types and fee record management APIs")
public class FeeController {

    private final FeeService feeService;

    // Fee Types
    @GetMapping("/fee-types")
    @Operation(summary = "Get all fee types")
    public ResponseEntity<ApiResponse<List<FeeTypeResponse>>> getFeeTypes(
            @RequestParam(defaultValue = "false") boolean activeOnly) {
        return ResponseEntity.ok(ApiResponse.success(activeOnly ? feeService.getActiveFeeTypes() : feeService.getAllFeeTypes()));
    }

    @PostMapping("/fee-types")
    @Operation(summary = "Create a fee type")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FeeTypeResponse>> createFeeType(@Valid @RequestBody CreateFeeTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Fee type created", feeService.createFeeType(request)));
    }

    @PatchMapping("/fee-types/{id}/toggle")
    @Operation(summary = "Toggle fee type active status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FeeTypeResponse>> toggleFeeType(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Fee type toggled", feeService.toggleFeeType(id)));
    }

    // Fee Records
    @GetMapping("/fees/student/{studentId}")
    @Operation(summary = "Get fee records for a student")
    @PreAuthorize("hasAnyRole('ADMIN','STUDENT')")
    public ResponseEntity<ApiResponse<List<FeeRecordResponse>>> getFeesByStudent(
            @PathVariable UUID studentId,
            @RequestParam(required = false) String academicYear) {
        List<FeeRecordResponse> fees = academicYear != null
                ? feeService.getFeesByStudentAndYear(studentId, academicYear)
                : feeService.getFeesByStudent(studentId);
        return ResponseEntity.ok(ApiResponse.success(fees));
    }

    @GetMapping("/fees/status/{status}")
    @Operation(summary = "Get all fee records with a given status (overdue, pending, etc.)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<FeeRecordResponse>>> getFeesByStatus(@PathVariable FeeStatus status) {
        return ResponseEntity.ok(ApiResponse.success(feeService.getFeesByStatus(status)));
    }

    @GetMapping("/fees/{id}")
    @Operation(summary = "Get fee record by ID")
    @PreAuthorize("hasAnyRole('ADMIN','STUDENT')")
    public ResponseEntity<ApiResponse<FeeRecordResponse>> getFeeById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(feeService.getFeeById(id)));
    }

    @PostMapping("/fees")
    @Operation(summary = "Create a fee record for a student")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FeeRecordResponse>> createFeeRecord(@Valid @RequestBody CreateFeeRecordRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Fee record created", feeService.createFeeRecord(request)));
    }

    @PutMapping("/fees/{id}")
    @Operation(summary = "Update fee record (record payment, update status)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FeeRecordResponse>> updateFeeRecord(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateFeeRecordRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Fee record updated", feeService.updateFeeRecord(id, request)));
    }
}

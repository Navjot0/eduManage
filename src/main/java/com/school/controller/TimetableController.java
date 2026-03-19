package com.school.controller;

import com.school.dto.request.CreateTimetableSlotRequest;
import com.school.dto.response.ApiResponse;
import com.school.dto.response.TimetableSlotResponse;
import com.school.enums.WeekdayEnum;
import com.school.service.impl.TimetableService;
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
@RequestMapping("/timetable")
@RequiredArgsConstructor
@Tag(name = "Timetable", description = "Timetable slot management APIs")
public class TimetableController {

    private final TimetableService timetableService;

    @GetMapping("/class")
    @Operation(summary = "Get class timetable (optionally filter by day)")
    public ResponseEntity<ApiResponse<List<TimetableSlotResponse>>> getClassTimetable(
            @RequestParam String className,
            @RequestParam String section,
            @RequestParam String academicYear,
            @RequestParam(required = false) WeekdayEnum day) {
        List<TimetableSlotResponse> slots = day != null
                ? timetableService.getTimetableByClassAndDay(className, section, day, academicYear)
                : timetableService.getTimetableByClass(className, section, academicYear);
        return ResponseEntity.ok(ApiResponse.success(slots));
    }

    @GetMapping("/teacher/{teacherId}")
    @Operation(summary = "Get a teacher's timetable (optionally filter by day)")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<ApiResponse<List<TimetableSlotResponse>>> getTeacherTimetable(
            @PathVariable UUID teacherId,
            @RequestParam(required = false) WeekdayEnum day) {
        List<TimetableSlotResponse> slots = day != null
                ? timetableService.getTeacherTimetableByDay(teacherId, day)
                : timetableService.getTeacherTimetable(teacherId);
        return ResponseEntity.ok(ApiResponse.success(slots));
    }

    @PostMapping
    @Operation(summary = "Create a timetable slot")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TimetableSlotResponse>> createSlot(@Valid @RequestBody CreateTimetableSlotRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Slot created", timetableService.createSlot(request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove a timetable slot (soft delete)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteSlot(@PathVariable UUID id) {
        timetableService.deleteSlot(id);
        return ResponseEntity.ok(ApiResponse.success("Slot removed", null));
    }
}

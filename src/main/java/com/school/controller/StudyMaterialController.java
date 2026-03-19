package com.school.controller;

import com.school.dto.request.CreateStudyMaterialRequest;
import com.school.dto.response.ApiResponse;
import com.school.dto.response.StudyMaterialResponse;
import com.school.security.UserPrincipal;
import com.school.service.impl.StudyMaterialService;
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
@RequestMapping("/study-materials")
@RequiredArgsConstructor
@Tag(name = "Study Materials", description = "Study material management APIs")
public class StudyMaterialController {

    private final StudyMaterialService materialService;

    @GetMapping
    @Operation(summary = "Get study materials by class, optionally filter by subject")
    public ResponseEntity<ApiResponse<List<StudyMaterialResponse>>> getMaterials(
            @RequestParam String className,
            @RequestParam String section,
            @RequestParam(required = false) String subject) {
        List<StudyMaterialResponse> materials = subject != null
                ? materialService.getMaterialsByClassAndSubject(className, section, subject)
                : materialService.getMaterialsByClass(className, section);
        return ResponseEntity.ok(ApiResponse.success(materials));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get study material by ID")
    public ResponseEntity<ApiResponse<StudyMaterialResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(materialService.getMaterialById(id)));
    }

    @GetMapping("/teacher/{teacherId}")
    @Operation(summary = "Get all materials uploaded by a teacher")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<ApiResponse<List<StudyMaterialResponse>>> getByTeacher(@PathVariable UUID teacherId) {
        return ResponseEntity.ok(ApiResponse.success(materialService.getMaterialsByTeacher(teacherId)));
    }

    @PostMapping
    @Operation(summary = "Upload a new study material")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<ApiResponse<StudyMaterialResponse>> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateStudyMaterialRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Material uploaded",
                        materialService.createMaterial(UUID.fromString(principal.getUserId()), request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove a study material (soft delete)")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        materialService.deleteMaterial(id);
        return ResponseEntity.ok(ApiResponse.success("Material removed", null));
    }
}

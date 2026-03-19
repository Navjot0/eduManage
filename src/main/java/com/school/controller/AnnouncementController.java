package com.school.controller;

import com.school.dto.request.CreateAnnouncementRequest;
import com.school.dto.response.AnnouncementResponse;
import com.school.dto.response.ApiResponse;
import com.school.enums.UserRole;
import com.school.security.UserPrincipal;
import com.school.service.impl.AnnouncementService;
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
@RequestMapping("/announcements")
@RequiredArgsConstructor
@Tag(name = "Announcements", description = "Announcement management APIs")
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @GetMapping
    @Operation(summary = "Get announcements for the logged-in user's role")
    public ResponseEntity<ApiResponse<List<AnnouncementResponse>>> getAnnouncements(
            @AuthenticationPrincipal UserPrincipal principal) {
        UserRole role = UserRole.valueOf(principal.getRole().toLowerCase());
        return ResponseEntity.ok(ApiResponse.success(announcementService.getAnnouncementsForRole(role)));
    }

    @GetMapping("/all")
    @Operation(summary = "Get all active announcements (admin)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AnnouncementResponse>>> getAllAnnouncements() {
        return ResponseEntity.ok(ApiResponse.success(announcementService.getAllActive()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get announcement by ID")
    public ResponseEntity<ApiResponse<AnnouncementResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(announcementService.getById(id)));
    }

    @PostMapping
    @Operation(summary = "Create a new announcement")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<ApiResponse<AnnouncementResponse>> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateAnnouncementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Announcement created",
                        announcementService.createAnnouncement(UUID.fromString(principal.getUserId()), request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an announcement")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<ApiResponse<AnnouncementResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateAnnouncementRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Announcement updated",
                announcementService.updateAnnouncement(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate an announcement")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        announcementService.deleteAnnouncement(id);
        return ResponseEntity.ok(ApiResponse.success("Announcement removed", null));
    }

    @PostMapping("/{id}/read")
    @Operation(summary = "Mark announcement as read by current user")
    public ResponseEntity<ApiResponse<Void>> markRead(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal) {
        announcementService.markAsRead(id, UUID.fromString(principal.getUserId()));
        return ResponseEntity.ok(ApiResponse.success("Marked as read", null));
    }
}

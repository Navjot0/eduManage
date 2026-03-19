package com.school.service.impl;

import com.school.dto.request.CreateAnnouncementRequest;
import com.school.dto.response.AnnouncementResponse;
import com.school.entity.Announcement;
import com.school.entity.AnnouncementRead;
import com.school.entity.User;
import com.school.enums.AnnouncementTarget;
import com.school.enums.UserRole;
import com.school.exception.ResourceNotFoundException;
import com.school.repository.AnnouncementReadRepository;
import com.school.repository.AnnouncementRepository;
import com.school.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final AnnouncementReadRepository readRepository;
    private final UserRepository userRepository;

    public List<AnnouncementResponse> getAllActive() {
        return announcementRepository.findByIsActiveOrderByCreatedAtDesc(true)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<AnnouncementResponse> getAnnouncementsForRole(UserRole role) {
        AnnouncementTarget roleTarget = AnnouncementTarget.valueOf(role.name());
        List<AnnouncementTarget> targets = List.of(AnnouncementTarget.all, roleTarget);
        return announcementRepository.findByTargetRoleInAndIsActiveOrderByCreatedAtDesc(targets, true)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public AnnouncementResponse getById(UUID id) {
        return mapToResponse(findById(id));
    }

    @Transactional
    public AnnouncementResponse createAnnouncement(UUID authorId, CreateAnnouncementRequest request) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", authorId));
        Announcement announcement = Announcement.builder()
                .title(request.getTitle()).content(request.getContent())
                .author(author).authorName(author.getName())
                .targetRole(request.getTargetRole()).priority(request.getPriority())
                .isActive(true)
                .build();
        return mapToResponse(announcementRepository.save(announcement));
    }

    @Transactional
    public AnnouncementResponse updateAnnouncement(UUID id, CreateAnnouncementRequest request) {
        Announcement announcement = findById(id);
        announcement.setTitle(request.getTitle());
        announcement.setContent(request.getContent());
        announcement.setTargetRole(request.getTargetRole());
        announcement.setPriority(request.getPriority());
        return mapToResponse(announcementRepository.save(announcement));
    }

    @Transactional
    public void deleteAnnouncement(UUID id) {
        Announcement announcement = findById(id);
        announcement.setIsActive(false);
        announcementRepository.save(announcement);
    }

    @Transactional
    public void markAsRead(UUID announcementId, UUID userId) {
        if (!readRepository.existsByAnnouncementIdAndUserId(announcementId, userId)) {
            Announcement announcement = findById(announcementId);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
            readRepository.save(AnnouncementRead.builder()
                    .announcement(announcement).user(user).build());
        }
    }

    private Announcement findById(UUID id) {
        return announcementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement", "id", id));
    }

    private AnnouncementResponse mapToResponse(Announcement a) {
        return AnnouncementResponse.builder()
                .id(a.getId()).title(a.getTitle()).content(a.getContent())
                .authorId(a.getAuthor().getId()).authorName(a.getAuthorName())
                .targetRole(a.getTargetRole()).priority(a.getPriority())
                .isActive(a.getIsActive())
                .readCount(readRepository.countByAnnouncementId(a.getId()))
                .createdAt(a.getCreatedAt()).updatedAt(a.getUpdatedAt())
                .build();
    }
}

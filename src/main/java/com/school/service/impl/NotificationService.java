package com.school.service.impl;

import com.school.dto.request.CreateNotificationRequest;
import com.school.dto.response.NotificationResponse;
import com.school.entity.Notification;
import com.school.entity.User;
import com.school.exception.ResourceNotFoundException;
import com.school.repository.NotificationRepository;
import com.school.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public List<NotificationResponse> getNotificationsForUser(UUID userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<NotificationResponse> getUnreadNotifications(UUID userId) {
        return notificationRepository.findByUserIdAndIsRead(userId, false)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public long getUnreadCount(UUID userId) {
        return notificationRepository.countByUserIdAndIsRead(userId, false);
    }

    @Transactional
    public NotificationResponse createNotification(CreateNotificationRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));
        Notification notification = Notification.builder()
                .user(user).title(request.getTitle()).message(request.getMessage())
                .type(request.getType()).isRead(false)
                .referenceType(request.getReferenceType()).referenceId(request.getReferenceId())
                .build();
        return mapToResponse(notificationRepository.save(notification));
    }

    @Transactional
    public NotificationResponse markAsRead(UUID id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id));
        notification.setIsRead(true);
        return mapToResponse(notificationRepository.save(notification));
    }

    @Transactional
    public int markAllAsRead(UUID userId) {
        return notificationRepository.markAllAsRead(userId);
    }

    @Transactional
    public void deleteNotification(UUID id) {
        if (!notificationRepository.existsById(id)) throw new ResourceNotFoundException("Notification", "id", id);
        notificationRepository.deleteById(id);
    }

    private NotificationResponse mapToResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId()).title(n.getTitle()).message(n.getMessage())
                .type(n.getType()).isRead(n.getIsRead())
                .referenceType(n.getReferenceType()).referenceId(n.getReferenceId())
                .createdAt(n.getCreatedAt())
                .build();
    }
}

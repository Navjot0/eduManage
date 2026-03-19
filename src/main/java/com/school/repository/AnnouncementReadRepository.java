package com.school.repository;

import com.school.entity.AnnouncementRead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AnnouncementReadRepository extends JpaRepository<AnnouncementRead, UUID> {
    boolean existsByAnnouncementIdAndUserId(UUID announcementId, UUID userId);
    Optional<AnnouncementRead> findByAnnouncementIdAndUserId(UUID announcementId, UUID userId);
    long countByAnnouncementId(UUID announcementId);
}

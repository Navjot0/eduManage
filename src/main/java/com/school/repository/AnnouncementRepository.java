package com.school.repository;

import com.school.entity.Announcement;
import com.school.enums.AnnouncementTarget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, UUID> {
    List<Announcement> findByIsActiveOrderByCreatedAtDesc(Boolean isActive);
    List<Announcement> findByTargetRoleInAndIsActiveOrderByCreatedAtDesc(List<AnnouncementTarget> targets, Boolean isActive);
    List<Announcement> findByAuthorId(UUID authorId);
}

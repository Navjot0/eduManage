package com.school.entity;

import com.school.enums.AnnouncementTarget;
import com.school.enums.PriorityLevel;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "announcements")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Announcement {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(name = "author_name", nullable = false, length = 100)
    private String authorName;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_role")
    private AnnouncementTarget targetRole = AnnouncementTarget.all;

    @Enumerated(EnumType.STRING)
    private PriorityLevel priority = PriorityLevel.medium;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreationTimestamp                                        // Hibernate sets this on INSERT only
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;                         // no Java initializer needed

    @UpdateTimestamp                                         // Hibernate sets this on INSERT + UPDATE
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

}

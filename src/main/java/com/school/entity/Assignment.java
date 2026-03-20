package com.school.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "assignments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 100)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "class_name", nullable = false, length = 20)
    private String className;

    @Column(nullable = false, length = 10)
    private String section;

    @ManyToOne
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @Column(name = "due_date", nullable = false)
    private LocalDateTime dueDate;

    @Column(name = "max_marks")
    private Integer maxMarks;

    @Column(name = "file_url", length = 500)
    private String fileUrl;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreationTimestamp                                        // Hibernate sets this on INSERT only
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;                         // no Java initializer needed

    @UpdateTimestamp                                         // Hibernate sets this on INSERT + UPDATE
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}

package com.school.entity;

import com.school.enums.MaterialType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "study_materials")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StudyMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 100)
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", nullable = false)
    private MaterialType fileType;

    @Column(name = "file_url", nullable = false, length = 500)
    private String fileUrl;

    @Column(name = "file_size_kb")
    private Integer fileSizeKb;

    @Column(name = "class_name", nullable = false, length = 20)
    private String className;

    @Column(length = 10)
    private String section;

    @ManyToOne
    @JoinColumn(name = "uploaded_by", nullable = false)
    private Teacher uploadedBy;

    @Column(name = "academic_year", nullable = false, length = 10)
    private String academicYear;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "uploaded_at", updatable = false)
    private LocalDateTime uploadedAt = LocalDateTime.now();
}

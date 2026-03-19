package com.school.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "classes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"class_name", "section", "academic_year"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Class {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "class_name", nullable = false, length = 20)
    private String className;

    @Column(nullable = false, length = 5)
    private String section;

    @Column(name = "academic_year", nullable = false, length = 10)
    private String academicYear;

    @ManyToOne
    @JoinColumn(name = "class_teacher_id")
    private Teacher classTeacher;

    @Column(name = "student_count")
    private Integer studentCount = 0;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}

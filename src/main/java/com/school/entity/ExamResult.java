package com.school.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "exam_results", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"exam_id", "student_id", "subject"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ExamResult {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(nullable = false, length = 100)
    private String subject;

    @Column(name = "max_marks", nullable = false)
    private Integer maxMarks;

    @Column(name = "obtained_marks", nullable = false)
    private Integer obtainedMarks;

    @Column(nullable = false, length = 5)
    private String grade;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "exam_date", nullable = false)
    private LocalDate examDate;

    @ManyToOne
    @JoinColumn(name = "entered_by")
    private Teacher enteredBy;

    @CreationTimestamp                                        // Hibernate sets this on INSERT only
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;                         // no Java initializer needed

    @UpdateTimestamp                                         // Hibernate sets this on INSERT + UPDATE
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}

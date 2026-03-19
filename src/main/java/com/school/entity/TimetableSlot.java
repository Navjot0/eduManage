package com.school.entity;

import com.school.enums.WeekdayEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "timetable_slots", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"class_name", "section", "day", "start_time", "academic_year"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TimetableSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "class_name", nullable = false, length = 20)
    private String className;

    @Column(nullable = false, length = 5)
    private String section;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WeekdayEnum day;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(nullable = false, length = 100)
    private String subject;

    @ManyToOne
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @Column(length = 50)
    private String room;

    @Column(name = "academic_year", nullable = false, length = 10)
    private String academicYear;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}

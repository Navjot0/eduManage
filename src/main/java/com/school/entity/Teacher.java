package com.school.entity;

import com.school.enums.StatusActive;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "teachers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Teacher {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Column(name = "employee_id", unique = true, nullable = false, length = 30)
    private String employeeId;

    @Column(nullable = false, length = 100)
    private String subject;

    @Column(length = 100)
    private String department;

    @Column(length = 200)
    private String qualification;

    @Column(name = "joining_date")
    private LocalDate joiningDate;

    @Enumerated(EnumType.STRING)
    private StatusActive status = StatusActive.active;

    @CreationTimestamp                                        // Hibernate sets this on INSERT only
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;                         // no Java initializer needed

    @UpdateTimestamp                                         // Hibernate sets this on INSERT + UPDATE
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}


package com.school.repository;

import com.school.entity.Student;
import com.school.enums.StatusActive;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentRepository extends JpaRepository<Student, UUID> {
    Optional<Student> findByUserId(UUID userId);
    Optional<Student> findByRollNumber(String rollNumber);
    List<Student> findByClassNameAndSection(String className, String section);
    List<Student> findByStatus(StatusActive status);
    boolean existsByRollNumber(String rollNumber);
}

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

    // Global lookup by roll number (kept for backward compat)
    Optional<Student> findByRollNumber(String rollNumber);

    // Per-class roll number lookup and validation
    Optional<Student> findByRollNumberAndClassNameAndSection(
            String rollNumber, String className, String section);

    boolean existsByRollNumberAndClassNameAndSection(
            String rollNumber, String className, String section);

    // Duplicate check excluding a specific student (for update validation)
    @org.springframework.data.jpa.repository.Query(
            "SELECT COUNT(s) > 0 FROM Student s WHERE s.rollNumber = :rollNumber " +
                    "AND s.className = :className AND s.section = :section AND s.id <> :excludeId")
    boolean existsByRollNumberAndClassAndSectionExcluding(
            @org.springframework.data.repository.query.Param("rollNumber") String rollNumber,
            @org.springframework.data.repository.query.Param("className") String className,
            @org.springframework.data.repository.query.Param("section") String section,
            @org.springframework.data.repository.query.Param("excludeId") java.util.UUID excludeId);

    // Get max roll number in a class to auto-assign next one
    @org.springframework.data.jpa.repository.Query(
            "SELECT s.rollNumber FROM Student s WHERE s.className = :className AND s.section = :section " +
                    "ORDER BY CAST(s.rollNumber AS integer) DESC")
    java.util.List<String> findRollNumbersByClassOrderedDesc(
            @org.springframework.data.repository.query.Param("className") String className,
            @org.springframework.data.repository.query.Param("section") String section);

    List<Student> findByClassNameAndSection(String className, String section);
    List<Student> findByStatus(StatusActive status);

    List<Student> findByClassNameAndSectionAndStatus(String className, String section, StatusActive status);

    List<Student> findByIdIn(List<UUID> ids);

    @org.springframework.data.jpa.repository.Query(
            "SELECT COUNT(s) FROM Student s WHERE s.className = :className AND s.section = :section AND s.status = 'active'")
    long countActiveByClassAndSection(
            @org.springframework.data.repository.query.Param("className") String className,
            @org.springframework.data.repository.query.Param("section") String section);
}
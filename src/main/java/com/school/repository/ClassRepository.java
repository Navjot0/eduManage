package com.school.repository;

import com.school.entity.Class;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClassRepository extends JpaRepository<Class, UUID> {
    Optional<Class> findByClassNameAndSectionAndAcademicYear(String className, String section, String academicYear);
    List<Class> findByAcademicYear(String academicYear);
    List<Class> findByClassTeacherId(UUID teacherId);
    List<Class> findByClassNameAndSection(String className, String section);

    // Increment student count atomically
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query(
            "UPDATE Class c SET c.studentCount = c.studentCount + 1 WHERE c.className = :className AND c.section = :section")
    int incrementStudentCount(@org.springframework.data.repository.query.Param("className") String className,
                              @org.springframework.data.repository.query.Param("section") String section);

    // Decrement student count atomically (min 0)
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query(
            "UPDATE Class c SET c.studentCount = GREATEST(c.studentCount - 1, 0) WHERE c.className = :className AND c.section = :section")
    int decrementStudentCount(@org.springframework.data.repository.query.Param("className") String className,
                              @org.springframework.data.repository.query.Param("section") String section);

    // Sync count to actual number of active students
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query(
            "UPDATE Class c SET c.studentCount = (SELECT COUNT(s) FROM Student s WHERE s.className = c.className AND s.section = c.section AND s.status = 'active') WHERE c.className = :className AND c.section = :section")
    int syncStudentCount(@org.springframework.data.repository.query.Param("className") String className,
                         @org.springframework.data.repository.query.Param("section") String section);
}
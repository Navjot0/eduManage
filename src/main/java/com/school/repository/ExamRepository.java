package com.school.repository;

import com.school.entity.Exam;
import com.school.enums.ExamStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExamRepository extends JpaRepository<Exam, UUID> {
    List<Exam> findByClassNameAndAcademicYear(String className, String academicYear);
    List<Exam> findByStatus(ExamStatus status);
    List<Exam> findByAcademicYear(String academicYear);
}

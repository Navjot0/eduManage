package com.school.repository;

import com.school.entity.ExamResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExamResultRepository extends JpaRepository<ExamResult, UUID> {
    List<ExamResult> findByStudentId(UUID studentId);
    List<ExamResult> findByExamId(UUID examId);
    List<ExamResult> findByStudentIdAndExamId(UUID studentId, UUID examId);
    Optional<ExamResult> findByExamIdAndStudentIdAndSubject(UUID examId, UUID studentId, String subject);
}

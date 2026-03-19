package com.school.repository;

import com.school.entity.AssignmentSubmission;
import com.school.enums.AssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AssignmentSubmissionRepository extends JpaRepository<AssignmentSubmission, UUID> {
    List<AssignmentSubmission> findByStudentId(UUID studentId);
    List<AssignmentSubmission> findByAssignmentId(UUID assignmentId);
    Optional<AssignmentSubmission> findByAssignmentIdAndStudentId(UUID assignmentId, UUID studentId);
    List<AssignmentSubmission> findByStudentIdAndStatus(UUID studentId, AssignmentStatus status);
}

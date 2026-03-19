package com.school.repository;

import com.school.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, UUID> {
    List<Assignment> findByClassNameAndSection(String className, String section);
    List<Assignment> findByTeacherId(UUID teacherId);
    List<Assignment> findByClassNameAndSectionAndIsActive(String className, String section, Boolean isActive);
}

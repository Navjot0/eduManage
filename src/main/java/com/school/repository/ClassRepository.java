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
}

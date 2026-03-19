package com.school.repository;

import com.school.entity.StudyMaterial;
import com.school.enums.MaterialType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StudyMaterialRepository extends JpaRepository<StudyMaterial, UUID> {
    List<StudyMaterial> findByClassNameAndSection(String className, String section);
    List<StudyMaterial> findByClassNameAndSectionAndSubject(String className, String section, String subject);
    List<StudyMaterial> findByUploadedById(UUID teacherId);
    List<StudyMaterial> findByFileType(MaterialType fileType);
    List<StudyMaterial> findByClassNameAndSectionAndIsActive(String className, String section, Boolean isActive);
}

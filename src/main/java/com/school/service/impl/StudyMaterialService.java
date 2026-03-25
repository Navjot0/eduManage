package com.school.service.impl;

import com.school.dto.request.CreateStudyMaterialRequest;
import com.school.dto.response.StudyMaterialResponse;
import com.school.entity.StudyMaterial;
import com.school.entity.Teacher;
import com.school.exception.ResourceNotFoundException;
import com.school.repository.ClassRepository;
import com.school.repository.StudyMaterialRepository;
import com.school.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudyMaterialService {

    private final ClassRepository classRepository;
    private final StudyMaterialRepository materialRepository;
    private final TeacherRepository teacherRepository;

    public List<StudyMaterialResponse> getMaterialsByClass(String className, String section) {
        return materialRepository.findByClassNameAndSectionAndIsActive(className, section, true)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<StudyMaterialResponse> getMaterialsByClassAndSubject(String className, String section, String subject) {
        return materialRepository.findByClassNameAndSectionAndSubject(className, section, subject)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<StudyMaterialResponse> getMaterialsByTeacher(UUID teacherId) {
        return materialRepository.findByUploadedById(teacherId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public StudyMaterialResponse getMaterialById(UUID id) {
        return mapToResponse(findById(id));
    }

    @Transactional
    public StudyMaterialResponse createMaterial(UUID userId, CreateStudyMaterialRequest request) {
        Teacher teacher = teacherRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher profile not found for userId", "userId", userId));
        // Validate class exists
        if (classRepository.findByClassNameAndSection(request.getClassName(), request.getSection()).isEmpty()) {
            throw new com.school.exception.BadRequestException(
                    "Class '" + request.getClassName() + "-" + request.getSection() + "' does not exist.");
        }

        StudyMaterial material = StudyMaterial.builder()
                .title(request.getTitle()).subject(request.getSubject())
                .description(request.getDescription()).fileType(request.getFileType())
                .fileUrl(request.getFileUrl()).fileSizeKb(request.getFileSizeKb())
                .className(request.getClassName()).section(request.getSection())
                .uploadedBy(teacher).academicYear(request.getAcademicYear()).isActive(true)
                .build();
        return mapToResponse(materialRepository.save(material));
    }

    @Transactional
    public void deleteMaterial(UUID id) {
        StudyMaterial material = findById(id);
        material.setIsActive(false);
        materialRepository.save(material);
    }

    private StudyMaterial findById(UUID id) {
        return materialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StudyMaterial", "id", id));
    }

    private StudyMaterialResponse mapToResponse(StudyMaterial m) {
        return StudyMaterialResponse.builder()
                .id(m.getId()).title(m.getTitle()).subject(m.getSubject())
                .description(m.getDescription()).fileType(m.getFileType())
                .fileUrl(m.getFileUrl()).fileSizeKb(m.getFileSizeKb())
                .className(m.getClassName()).section(m.getSection())
                .uploadedById(m.getUploadedBy().getId())
                .uploadedByName(m.getUploadedBy().getUser().getName())
                .academicYear(m.getAcademicYear()).isActive(m.getIsActive())
                .uploadedAt(m.getUploadedAt())
                .build();
    }
}
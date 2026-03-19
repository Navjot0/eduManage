package com.school.service.impl;

import com.school.dto.request.CreateClassRequest;
import com.school.dto.response.ClassResponse;
import com.school.entity.Class;
import com.school.entity.Teacher;
import com.school.exception.ConflictException;
import com.school.exception.ResourceNotFoundException;
import com.school.repository.ClassRepository;
import com.school.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClassService {

    private final ClassRepository classRepository;
    private final TeacherRepository teacherRepository;

    public List<ClassResponse> getAllClasses() {
        return classRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<ClassResponse> getClassesByAcademicYear(String academicYear) {
        return classRepository.findByAcademicYear(academicYear).stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public ClassResponse getClassById(UUID id) {
        return mapToResponse(findById(id));
    }

    @Transactional
    public ClassResponse createClass(CreateClassRequest request) {
        classRepository.findByClassNameAndSectionAndAcademicYear(
                request.getClassName(), request.getSection(), request.getAcademicYear())
                .ifPresent(c -> { throw new ConflictException("Class already exists for this academic year"); });

        Teacher teacher = null;
        if (request.getClassTeacherId() != null) {
            teacher = teacherRepository.findById(request.getClassTeacherId())
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", request.getClassTeacherId()));
        }

        Class cls = Class.builder()
                .className(request.getClassName())
                .section(request.getSection())
                .academicYear(request.getAcademicYear())
                .classTeacher(teacher)
                .studentCount(0)
                .build();
        return mapToResponse(classRepository.save(cls));
    }

    @Transactional
    public ClassResponse assignTeacher(UUID classId, UUID teacherId) {
        Class cls = findById(classId);
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", teacherId));
        cls.setClassTeacher(teacher);
        return mapToResponse(classRepository.save(cls));
    }

    @Transactional
    public void deleteClass(UUID id) {
        if (!classRepository.existsById(id)) throw new ResourceNotFoundException("Class", "id", id);
        classRepository.deleteById(id);
    }

    private Class findById(UUID id) {
        return classRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Class", "id", id));
    }

    private ClassResponse mapToResponse(Class c) {
        return ClassResponse.builder()
                .id(c.getId())
                .className(c.getClassName())
                .section(c.getSection())
                .academicYear(c.getAcademicYear())
                .classTeacherId(c.getClassTeacher() != null ? c.getClassTeacher().getId() : null)
                .classTeacherName(c.getClassTeacher() != null ? c.getClassTeacher().getUser().getName() : null)
                .studentCount(c.getStudentCount())
                .createdAt(c.getCreatedAt())
                .build();
    }
}

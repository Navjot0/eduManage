package com.school.service.impl;

import com.school.dto.request.CreateClassRequest;
import com.school.dto.response.ClassResponse;
import com.school.entity.Class;
import com.school.entity.Teacher;
import com.school.exception.ConflictException;
import com.school.exception.ResourceNotFoundException;
import com.school.dto.response.TeacherDashboardResponse;
import com.school.dto.response.TeacherDashboardResponse.*;
import com.school.entity.TimetableSlot;
import com.school.repository.ClassRepository;
import com.school.repository.StudentRepository;
import com.school.repository.TimetableRepository;
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
    private final TimetableRepository timetableRepository;
    private final StudentRepository studentRepository;

    public List<ClassResponse> getAllClasses() {
        return classRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<ClassResponse> getClassesByAcademicYear(String academicYear) {
        return classRepository.findByAcademicYear(academicYear).stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public java.util.List<ClassResponse> getClassesByNameAndSection(String className, String section) {
        return classRepository.findByClassNameAndSection(className, section)
                .stream().map(this::mapToResponse).collect(java.util.stream.Collectors.toList());
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

    @Transactional
    public ClassResponse syncStudentCount(UUID classId) {
        Class cls = findById(classId);
        classRepository.syncStudentCount(cls.getClassName(), cls.getSection());
        // Re-fetch after update
        return mapToResponse(findById(classId));
    }

    public TeacherDashboardResponse getTeacherDashboard(UUID teacherId, String academicYear) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new com.school.exception.ResourceNotFoundException("Teacher", "id", teacherId));

        // 1. Classes where teacher is the class teacher
        List<AssignedClassInfo> classTeacherOf = classRepository.findByClassTeacherId(teacherId)
                .stream()
                .filter(cls -> academicYear == null || cls.getAcademicYear().equals(academicYear))
                .map(cls -> AssignedClassInfo.builder()
                        .classId(cls.getId())
                        .className(cls.getClassName())
                        .section(cls.getSection())
                        .academicYear(cls.getAcademicYear())
                        .studentCount(cls.getStudentCount())
                        .role("Class Teacher")
                        .build())
                .collect(java.util.stream.Collectors.toList());

        // 2. Classes teacher teaches via timetable (distinct className+section combos)
        List<TimetableSlot> slots = academicYear != null
                ? timetableRepository.findByTeacherIdAndAcademicYear(teacherId, academicYear)
                : timetableRepository.findByTeacherId(teacherId);

        // Group slots by className + section + subject + academicYear
        java.util.Map<String, List<TimetableSlot>> grouped = slots.stream()
                .filter(s -> Boolean.TRUE.equals(s.getIsActive()))
                .collect(java.util.stream.Collectors.groupingBy(
                        s -> s.getClassName() + "~" + s.getSection() + "~" + s.getSubject() + "~" + s.getAcademicYear()
                ));

        List<TaughtClassInfo> teachingClasses = grouped.entrySet().stream()
                .map(entry -> {
                    String[] parts = entry.getKey().split("~");
                    String cn = parts[0], sec = parts[1], subj = parts[2], yr = parts[3];
                    long studentCount = studentRepository.countActiveByClassAndSection(cn, sec);

                    List<TimetableEntry> schedule = entry.getValue().stream()
                            .map(s -> TimetableEntry.builder()
                                    .day(s.getDay())
                                    .startTime(s.getStartTime())
                                    .endTime(s.getEndTime())
                                    .room(s.getRoom())
                                    .build())
                            .sorted(java.util.Comparator.comparing(TimetableEntry::getDay)
                                    .thenComparing(TimetableEntry::getStartTime))
                            .collect(java.util.stream.Collectors.toList());

                    return TaughtClassInfo.builder()
                            .className(cn).section(sec).subject(subj).academicYear(yr)
                            .studentCount((int) studentCount)
                            .schedule(schedule)
                            .build();
                })
                .sorted(java.util.Comparator.comparing(TaughtClassInfo::getClassName)
                        .thenComparing(TaughtClassInfo::getSection))
                .collect(java.util.stream.Collectors.toList());

        int totalStudents = classTeacherOf.stream().mapToInt(AssignedClassInfo::getStudentCount).sum();

        return TeacherDashboardResponse.builder()
                .teacherId(teacher.getId())
                .teacherName(teacher.getUser().getName())
                .employeeId(teacher.getEmployeeId())
                .subject(teacher.getSubject())
                .department(teacher.getDepartment())
                .classTeacherOf(classTeacherOf)
                .teachingClasses(teachingClasses)
                .totalClassesAsClassTeacher(classTeacherOf.size())
                .totalClassesTeaching(teachingClasses.size())
                .totalStudentsInCharge(totalStudents)
                .build();
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
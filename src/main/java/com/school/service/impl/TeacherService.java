package com.school.service.impl;

import com.school.dto.request.CreateTeacherRequest;
import com.school.dto.request.UpdateTeacherRequest;
import com.school.dto.response.TeacherResponse;
import com.school.entity.Teacher;
import com.school.entity.User;
import com.school.enums.StatusActive;
import com.school.enums.UserRole;
import com.school.exception.ConflictException;
import com.school.exception.ResourceNotFoundException;
import com.school.repository.TeacherRepository;
import com.school.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<TeacherResponse> getAllTeachers(StatusActive status) {
        return teacherRepository.findAll().stream()
                .filter(t -> status == null || t.getStatus() == status)
                .map(this::mapToResponse).collect(Collectors.toList());
    }

    public TeacherResponse getTeacherByUserId(UUID userId) {
        return mapToResponse(teacherRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "userId", userId)));
    }

    public TeacherResponse getTeacherById(UUID id) {
        return mapToResponse(findById(id));
    }

    public List<TeacherResponse> getTeachersByDepartment(String department, StatusActive status) {
        return teacherRepository.findByDepartment(department).stream()
                .filter(t -> status == null || t.getStatus() == status)
                .map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public TeacherResponse createTeacher(CreateTeacherRequest request) {
        if (userRepository.existsByEmail(request.getEmail()))
            throw new ConflictException("Email already registered: " + request.getEmail());
        if (teacherRepository.existsByEmployeeId(request.getEmployeeId()))
            throw new ConflictException("Employee ID already exists: " + request.getEmployeeId());

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.teacher)
                .phone(request.getPhone())
                .isActive(true)
                .build();
        user = userRepository.save(user);

        Teacher teacher = Teacher.builder()
                .id(UUID.randomUUID())
                .user(user)
                .employeeId(request.getEmployeeId())
                .subject(request.getSubject())
                .department(request.getDepartment())
                .qualification(request.getQualification())
                .joiningDate(request.getJoiningDate())
                .status(StatusActive.active)
                .build();
        return mapToResponse(teacherRepository.save(teacher));
    }

    @Transactional
    public TeacherResponse updateTeacher(UUID id, UpdateTeacherRequest request) {
        Teacher teacher = findById(id);
        if (request.getSubject() != null) teacher.setSubject(request.getSubject());
        if (request.getDepartment() != null) teacher.setDepartment(request.getDepartment());
        if (request.getQualification() != null) teacher.setQualification(request.getQualification());
        if (request.getStatus() != null) teacher.setStatus(request.getStatus());
        return mapToResponse(teacherRepository.save(teacher));
    }

    @Transactional
    public TeacherResponse toggleTeacherStatus(UUID id) {
        Teacher teacher = findById(id);
        StatusActive newStatus = teacher.getStatus() == StatusActive.active
                ? StatusActive.inactive : StatusActive.active;
        return setTeacherStatus(id, newStatus);
    }

    @Transactional
    public TeacherResponse setTeacherStatus(UUID id, StatusActive newStatus) {
        Teacher teacher = findById(id);
        teacher.setStatus(newStatus);

        // Sync the linked user account active flag
        teacher.getUser().setIsActive(newStatus == StatusActive.active);
        userRepository.save(teacher.getUser());

        return mapToResponse(teacherRepository.save(teacher));
    }

    @Transactional
    public void deleteTeacher(UUID id) {
        if (!teacherRepository.existsById(id)) throw new ResourceNotFoundException("Teacher", "id", id);
        teacherRepository.deleteById(id);
    }

    public Teacher findById(UUID id) {
        return teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", id));
    }

    public TeacherResponse mapToResponse(Teacher t) {
        return TeacherResponse.builder()
                .id(t.getId())
                .userId(t.getUser().getId())
                .name(t.getUser().getName())
                .email(t.getUser().getEmail())
                .employeeId(t.getEmployeeId())
                .subject(t.getSubject())
                .department(t.getDepartment())
                .qualification(t.getQualification())
                .joiningDate(t.getJoiningDate())
                .status(t.getStatus())
                .createdAt(t.getCreatedAt())
                .build();
    }
}
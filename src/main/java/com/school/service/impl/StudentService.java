package com.school.service.impl;

import com.school.dto.request.CreateStudentRequest;
import com.school.dto.request.UpdateStudentRequest;
import com.school.dto.response.StudentResponse;
import com.school.entity.Student;
import com.school.entity.User;
import com.school.enums.StatusActive;
import com.school.enums.UserRole;
import com.school.exception.ConflictException;
import com.school.exception.ResourceNotFoundException;
import com.school.repository.StudentRepository;
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
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<StudentResponse> getAllStudents() {
        return studentRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<StudentResponse> getStudentsByClass(String className, String section) {
        return studentRepository.findByClassNameAndSection(className, section)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public StudentResponse getStudentById(UUID id) {
        return mapToResponse(findById(id));
    }

    public StudentResponse getStudentByRollNumber(String rollNumber) {
        return mapToResponse(studentRepository.findByRollNumber(rollNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "rollNumber", rollNumber)));
    }

    @Transactional
    public StudentResponse createStudent(CreateStudentRequest request) {
        if (userRepository.existsByEmail(request.getEmail()))
            throw new ConflictException("Email already registered: " + request.getEmail());
        if (studentRepository.existsByRollNumber(request.getRollNumber()))
            throw new ConflictException("Roll number already exists: " + request.getRollNumber());

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.student)
                .phone(request.getPhone())
                .isActive(true)
                .build();
        user = userRepository.save(user);

        Student student = Student.builder()
                .id(UUID.randomUUID())
                .user(user)
                .rollNumber(request.getRollNumber())
                .className(request.getClassName())
                .section(request.getSection())
                .parentName(request.getParentName())
                .parentPhone(request.getParentPhone())
                .address(request.getAddress())
                .dateOfBirth(request.getDateOfBirth())
                .admissionDate(request.getAdmissionDate())
                .status(StatusActive.active)
                .build();
        return mapToResponse(studentRepository.save(student));
    }

    @Transactional
    public StudentResponse updateStudent(UUID id, UpdateStudentRequest request) {
        Student student = findById(id);
        if (request.getClassName() != null) student.setClassName(request.getClassName());
        if (request.getSection() != null) student.setSection(request.getSection());
        if (request.getParentName() != null) student.setParentName(request.getParentName());
        if (request.getParentPhone() != null) student.setParentPhone(request.getParentPhone());
        if (request.getAddress() != null) student.setAddress(request.getAddress());
        if (request.getDateOfBirth() != null) student.setDateOfBirth(request.getDateOfBirth());
        if (request.getStatus() != null) student.setStatus(request.getStatus());
        return mapToResponse(studentRepository.save(student));
    }

    @Transactional
    public void deleteStudent(UUID id) {
        if (!studentRepository.existsById(id)) throw new ResourceNotFoundException("Student", "id", id);
        studentRepository.deleteById(id);
    }

    public Student findById(UUID id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));
    }

    public StudentResponse mapToResponse(Student s) {
        return StudentResponse.builder()
                .id(s.getId())
                .userId(s.getUser().getId())
                .name(s.getUser().getName())
                .email(s.getUser().getEmail())
                .rollNumber(s.getRollNumber())
                .className(s.getClassName())
                .section(s.getSection())
                .parentName(s.getParentName())
                .parentPhone(s.getParentPhone())
                .address(s.getAddress())
                .dateOfBirth(s.getDateOfBirth())
                .admissionDate(s.getAdmissionDate())
                .status(s.getStatus())
                .createdAt(s.getCreatedAt())
                .build();
    }
}

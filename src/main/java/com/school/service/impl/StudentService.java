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
import com.school.entity.Class;
import com.school.repository.ClassRepository;
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
    private final ClassRepository classRepository;
    private final PasswordEncoder passwordEncoder;

    public List<StudentResponse> getAllStudents(StatusActive status) {
        return studentRepository.findAll().stream()
                .filter(s -> status == null || s.getStatus() == status)
                .map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<StudentResponse> getStudentsByClass(String className, String section, StatusActive status) {
        return studentRepository.findByClassNameAndSection(className, section).stream()
                .filter(s -> status == null || s.getStatus() == status)
                .map(this::mapToResponse).collect(Collectors.toList());
    }

    public StudentResponse getStudentByUserId(UUID userId) {
        return mapToResponse(studentRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "userId", userId)));
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
        StudentResponse response = mapToResponse(studentRepository.save(student));

        // Increment student_count in the matching class row
        int updated = classRepository.incrementStudentCount(request.getClassName(), request.getSection());
        if (updated == 0) {
            // Class row doesn't exist yet — that's OK, count stays at default
            // Admins should create the class first via POST /classes
        }

        return response;
    }

    @Transactional
    public StudentResponse updateStudent(UUID id, UpdateStudentRequest request) {
        Student student = findById(id);

        // Detect class/section change to update counts
        String oldClass   = student.getClassName();
        String oldSection = student.getSection();
        boolean classChanged = (request.getClassName() != null && !request.getClassName().equals(oldClass))
                || (request.getSection()   != null && !request.getSection().equals(oldSection));

        if (request.getClassName() != null) student.setClassName(request.getClassName());
        if (request.getSection()   != null) student.setSection(request.getSection());
        if (request.getParentName()  != null) student.setParentName(request.getParentName());
        if (request.getParentPhone() != null) student.setParentPhone(request.getParentPhone());
        if (request.getAddress()     != null) student.setAddress(request.getAddress());
        if (request.getDateOfBirth() != null) student.setDateOfBirth(request.getDateOfBirth());

        // Handle activation / deactivation affecting count
        if (request.getStatus() != null) {
            boolean wasActive = student.getStatus() == com.school.enums.StatusActive.active;
            boolean nowActive = request.getStatus() == com.school.enums.StatusActive.active;
            student.setStatus(request.getStatus());
            if (wasActive && !nowActive) {
                classRepository.decrementStudentCount(student.getClassName(), student.getSection());
            } else if (!wasActive && nowActive) {
                classRepository.incrementStudentCount(student.getClassName(), student.getSection());
            }
        }

        StudentResponse response = mapToResponse(studentRepository.save(student));

        // If moved to a different class, update both class counts
        if (classChanged) {
            classRepository.decrementStudentCount(oldClass, oldSection);
            classRepository.incrementStudentCount(student.getClassName(), student.getSection());
        }

        return response;
    }

    @Transactional
    public StudentResponse toggleStudentStatus(UUID id) {
        Student student = findById(id);
        StatusActive newStatus = student.getStatus() == StatusActive.active
                ? StatusActive.inactive : StatusActive.active;
        return setStudentStatus(id, newStatus);
    }

    @Transactional
    public StudentResponse setStudentStatus(UUID id, StatusActive newStatus) {
        Student student = findById(id);
        StatusActive oldStatus = student.getStatus();

        student.setStatus(newStatus);

        // Sync the linked user account active flag
        User user = student.getUser();
        user.setIsActive(newStatus == StatusActive.active);
        userRepository.save(user);

        studentRepository.save(student);

        // Sync class student_count
        if (oldStatus != newStatus) {
            if (newStatus == StatusActive.active) {
                classRepository.incrementStudentCount(student.getClassName(), student.getSection());
            } else {
                classRepository.decrementStudentCount(student.getClassName(), student.getSection());
            }
        }

        return mapToResponse(student);
    }

    @Transactional
    public void deleteStudent(UUID id) {
        Student student = findById(id);
        String className = student.getClassName();
        String section   = student.getSection();
        boolean wasActive = student.getStatus() == com.school.enums.StatusActive.active;

        studentRepository.deleteById(id);

        // Only decrement if student was active
        if (wasActive) {
            classRepository.decrementStudentCount(className, section);
        }
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
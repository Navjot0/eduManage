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

    /** Preferred: look up by roll number scoped to a specific class */
    public StudentResponse getStudentByRollNumberAndClass(
            String rollNumber, String className, String section) {
        return mapToResponse(
                studentRepository.findByRollNumberAndClassNameAndSection(rollNumber, className, section)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Student", "rollNumber in " + className + "-" + section, rollNumber)));
    }

    @Transactional
    public StudentResponse createStudent(CreateStudentRequest request) {
        if (userRepository.existsByEmail(request.getEmail()))
            throw new ConflictException("Email already registered: " + request.getEmail());

        // Validate class exists
        if (classRepository.findByClassNameAndSection(request.getClassName(), request.getSection()).isEmpty())
            throw new com.school.exception.BadRequestException(
                    "Class '" + request.getClassName() + "-" + request.getSection() +
                            "' does not exist. Create the class first via POST /classes.");

        // Resolve roll number — auto-assign if not provided
        String rollNumber = resolveRollNumber(
                request.getRollNumber(), request.getClassName(), request.getSection(), null);

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
                .rollNumber(rollNumber)
                .className(request.getClassName())
                .section(request.getSection())
                .parentName(request.getParentName())
                .parentPhone(request.getParentPhone())
                .address(request.getAddress())
                .dateOfBirth(request.getDateOfBirth())
                .admissionDate(request.getAdmissionDate())
                .status(StatusActive.active)
                .build();
        // student_count updated automatically by DB trigger trg_sync_student_count
        return mapToResponse(studentRepository.save(student));
    }

    @Transactional
    public StudentResponse updateStudent(UUID id, UpdateStudentRequest request) {
        Student student = findById(id);

        String targetClass   = request.getClassName() != null ? request.getClassName() : student.getClassName();
        String targetSection = request.getSection()   != null ? request.getSection()   : student.getSection();
        boolean classChanged = !targetClass.equals(student.getClassName()) ||
                !targetSection.equals(student.getSection());

        // Validate target class exists if changing class
        if (classChanged && classRepository.findByClassNameAndSection(targetClass, targetSection).isEmpty())
            throw new com.school.exception.BadRequestException(
                    "Target class '" + targetClass + "-" + targetSection + "' does not exist.");

        // Resolve roll number for target class
        if (request.getRollNumber() != null || classChanged) {
            String newRoll = resolveRollNumber(
                    request.getRollNumber(), targetClass, targetSection, student.getId());
            student.setRollNumber(newRoll);
        }

        if (request.getClassName() != null) student.setClassName(request.getClassName());
        if (request.getSection()   != null) student.setSection(request.getSection());
        if (request.getParentName()  != null) student.setParentName(request.getParentName());
        if (request.getParentPhone() != null) student.setParentPhone(request.getParentPhone());
        if (request.getAddress()     != null) student.setAddress(request.getAddress());
        if (request.getDateOfBirth() != null) student.setDateOfBirth(request.getDateOfBirth());

        // status change — student_count updated by DB trigger
        if (request.getStatus() != null) student.setStatus(request.getStatus());

        return mapToResponse(studentRepository.save(student));
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

        // student_count updated automatically by DB trigger trg_sync_student_count
        studentRepository.save(student);
        return mapToResponse(student);
    }

    @Transactional
    public void deleteStudent(UUID id) {
        findById(id); // validate exists
        // student_count updated automatically by DB trigger trg_sync_student_count
        studentRepository.deleteById(id);
    }

    public Student findById(UUID id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));
    }

    /**
     * Resolves the roll number to use when creating or updating a student.
     *
     * Rules:
     *  - If rollNumber is provided: validate it is not already taken in className+section
     *    (excluding the current student on updates).
     *  - If rollNumber is null/blank: auto-assign the next available sequential number
     *    in the class (001, 002, 003...).
     */
    private String resolveRollNumber(String requested, String className,
                                     String section, UUID excludeStudentId) {
        if (requested != null && !requested.isBlank()) {
            // Validate uniqueness within class
            boolean taken = excludeStudentId != null
                    ? studentRepository.existsByRollNumberAndClassAndSectionExcluding(
                    requested, className, section, excludeStudentId)
                    : studentRepository.existsByRollNumberAndClassNameAndSection(
                    requested, className, section);
            if (taken)
                throw new ConflictException(
                        "Roll number '" + requested + "' is already taken in class " +
                                className + "-" + section);
            return requested;
        }

        // Auto-assign: find max roll number in the class and increment
        java.util.List<String> existing = studentRepository
                .findRollNumbersByClassOrderedDesc(className, section);

        if (existing.isEmpty()) {
            return "001";
        }

        // Parse the highest numeric roll number and add 1
        for (String roll : existing) {
            try {
                int next = Integer.parseInt(roll.trim()) + 1;
                return String.format("%03d", next);
            } catch (NumberFormatException ignored) {
                // skip non-numeric roll numbers
            }
        }
        // Fallback: total count + 1
        return String.format("%03d", existing.size() + 1);
    }

    public StudentResponse mapToResponse(Student s) {
        // Look up the linked class record to get classId, teacher, and student_count
        java.util.Optional<com.school.entity.Class> classOpt =
                classRepository.findByClassNameAndSection(s.getClassName(), s.getSection())
                        .stream().findFirst();

        UUID classId             = classOpt.map(com.school.entity.Class::getId).orElse(null);
        String classTeacherName  = classOpt
                .filter(cls -> cls.getClassTeacher() != null)
                .map(cls -> cls.getClassTeacher().getUser().getName())
                .orElse(null);
        Integer totalStudents    = classOpt.map(com.school.entity.Class::getStudentCount).orElse(null);
        String academicYear      = classOpt.map(com.school.entity.Class::getAcademicYear).orElse(null);

        return StudentResponse.builder()
                .id(s.getId())
                .userId(s.getUser().getId())
                .name(s.getUser().getName())
                .email(s.getUser().getEmail())
                .rollNumber(s.getRollNumber())
                .className(s.getClassName())
                .section(s.getSection())
                .academicYear(academicYear)
                .parentName(s.getParentName())
                .parentPhone(s.getParentPhone())
                .address(s.getAddress())
                .dateOfBirth(s.getDateOfBirth())
                .admissionDate(s.getAdmissionDate())
                .status(s.getStatus())
                .classId(classId)
                .classTeacherName(classTeacherName)
                .totalStudentsInClass(totalStudents)
                .createdAt(s.getCreatedAt())
                .build();
    }
}
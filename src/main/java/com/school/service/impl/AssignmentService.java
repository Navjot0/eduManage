package com.school.service.impl;

import com.school.dto.request.CreateAssignmentRequest;
import com.school.dto.request.GradeSubmissionRequest;
import com.school.dto.request.SubmitAssignmentRequest;
import com.school.dto.response.AssignmentResponse;
import com.school.dto.response.SubmissionResponse;
import com.school.entity.Assignment;
import com.school.entity.AssignmentSubmission;
import com.school.entity.Student;
import com.school.entity.Teacher;
import com.school.enums.AssignmentStatus;
import com.school.exception.BadRequestException;
import com.school.exception.ResourceNotFoundException;
import com.school.repository.AssignmentRepository;
import com.school.repository.AssignmentSubmissionRepository;
import com.school.repository.StudentRepository;
import com.school.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionRepository submissionRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;

    public List<AssignmentResponse> getAssignmentsByClass(String className, String section) {
        return assignmentRepository.findByClassNameAndSection(className, section)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<AssignmentResponse> getAssignmentsByTeacher(UUID teacherId) {
        return assignmentRepository.findByTeacherId(teacherId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public AssignmentResponse getAssignmentById(UUID id) {
        return mapToResponse(findAssignmentById(id));
    }

    @Transactional
    public AssignmentResponse createAssignment(UUID userId, CreateAssignmentRequest request) {
        Teacher teacher = teacherRepository.findByUserId(userId)
                .orElseGet(() -> teacherRepository.findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("Teacher", "userId", userId)));
        Assignment assignment = Assignment.builder()
                .title(request.getTitle()).subject(request.getSubject())
                .description(request.getDescription()).className(request.getClassName())
                .section(request.getSection()).teacher(teacher).dueDate(request.getDueDate())
                .maxMarks(request.getMaxMarks()).fileUrl(request.getFileUrl()).isActive(true)
                .build();
        return mapToResponse(assignmentRepository.save(assignment));
    }

    @Transactional
    public AssignmentResponse updateAssignment(UUID id, CreateAssignmentRequest request) {
        Assignment assignment = findAssignmentById(id);
        assignment.setTitle(request.getTitle());
        assignment.setDescription(request.getDescription());
        assignment.setDueDate(request.getDueDate());
        assignment.setMaxMarks(request.getMaxMarks());
        if (request.getFileUrl() != null) assignment.setFileUrl(request.getFileUrl());
        return mapToResponse(assignmentRepository.save(assignment));
    }

    @Transactional
    public void deleteAssignment(UUID id) {
        if (!assignmentRepository.existsById(id)) throw new ResourceNotFoundException("Assignment", "id", id);
        assignmentRepository.deleteById(id);
    }

    // Submissions
    public List<SubmissionResponse> getSubmissionsForAssignment(UUID assignmentId) {
        return submissionRepository.findByAssignmentId(assignmentId)
                .stream().map(this::mapSubmissionToResponse).collect(Collectors.toList());
    }

    public List<SubmissionResponse> getSubmissionsByStudent(UUID studentId) {
        return submissionRepository.findByStudentId(studentId)
                .stream().map(this::mapSubmissionToResponse).collect(Collectors.toList());
    }

    @Transactional
    public SubmissionResponse submitAssignment(UUID assignmentId, UUID studentId, SubmitAssignmentRequest request) {
        Assignment assignment = findAssignmentById(assignmentId);
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", studentId));

        submissionRepository.findByAssignmentIdAndStudentId(assignmentId, studentId).ifPresent(s -> {
            if (s.getStatus() == AssignmentStatus.submitted || s.getStatus() == AssignmentStatus.graded)
                throw new BadRequestException("Assignment already submitted");
        });

        AssignmentStatus status = LocalDateTime.now().isAfter(assignment.getDueDate())
                ? AssignmentStatus.overdue : AssignmentStatus.submitted;

        AssignmentSubmission submission = submissionRepository
                .findByAssignmentIdAndStudentId(assignmentId, studentId)
                .orElse(AssignmentSubmission.builder().assignment(assignment).student(student).build());
        submission.setStatus(status);
        submission.setSubmittedAt(LocalDateTime.now());
        submission.setFileUrl(request.getFileUrl());
        return mapSubmissionToResponse(submissionRepository.save(submission));
    }

    @Transactional
    public SubmissionResponse gradeSubmission(UUID submissionId, GradeSubmissionRequest request) {
        AssignmentSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission", "id", submissionId));
        submission.setObtainedMarks(request.getObtainedMarks());
        submission.setFeedback(request.getFeedback());
        submission.setStatus(AssignmentStatus.graded);
        submission.setGradedAt(LocalDateTime.now());
        return mapSubmissionToResponse(submissionRepository.save(submission));
    }

    private Assignment findAssignmentById(UUID id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment", "id", id));
    }

    private AssignmentResponse mapToResponse(Assignment a) {
        return AssignmentResponse.builder()
                .id(a.getId()).title(a.getTitle()).subject(a.getSubject())
                .description(a.getDescription()).className(a.getClassName()).section(a.getSection())
                .teacherId(a.getTeacher().getId()).teacherName(a.getTeacher().getUser().getName())
                .dueDate(a.getDueDate()).maxMarks(a.getMaxMarks()).fileUrl(a.getFileUrl())
                .isActive(a.getIsActive()).createdAt(a.getCreatedAt())
                .build();
    }

    private SubmissionResponse mapSubmissionToResponse(AssignmentSubmission s) {
        return SubmissionResponse.builder()
                .id(s.getId()).assignmentId(s.getAssignment().getId())
                .assignmentTitle(s.getAssignment().getTitle())
                .studentId(s.getStudent().getId()).studentName(s.getStudent().getUser().getName())
                .status(s.getStatus()).submittedAt(s.getSubmittedAt()).fileUrl(s.getFileUrl())
                .obtainedMarks(s.getObtainedMarks()).maxMarks(s.getAssignment().getMaxMarks())
                .feedback(s.getFeedback()).gradedAt(s.getGradedAt())
                .build();
    }
}

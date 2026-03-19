package com.school.service.impl;

import com.school.dto.request.CreateExamRequest;
import com.school.dto.request.CreateExamResultRequest;
import com.school.dto.response.ExamResponse;
import com.school.dto.response.ExamResultResponse;
import com.school.entity.Exam;
import com.school.entity.ExamResult;
import com.school.entity.Student;
import com.school.entity.Teacher;
import com.school.enums.ExamStatus;
import com.school.exception.ConflictException;
import com.school.exception.ResourceNotFoundException;
import com.school.repository.ExamRepository;
import com.school.repository.ExamResultRepository;
import com.school.repository.StudentRepository;
import com.school.repository.TeacherRepository;
import com.school.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamService {

    private final ExamRepository examRepository;
    private final ExamResultRepository resultRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;

    public List<ExamResponse> getAllExams() {
        return examRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<ExamResponse> getExamsByClass(String className, String academicYear) {
        return examRepository.findByClassNameAndAcademicYear(className, academicYear)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public ExamResponse getExamById(UUID id) {
        return mapToResponse(findById(id));
    }

    @Transactional
    public ExamResponse createExam(UUID createdByUserId, CreateExamRequest request) {
        var creator = userRepository.findById(createdByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", createdByUserId));
        Exam exam = Exam.builder()
                .name(request.getName()).className(request.getClassName())
                .section(request.getSection()).academicYear(request.getAcademicYear())
                .startDate(request.getStartDate()).endDate(request.getEndDate())
                .status(ExamStatus.upcoming).createdBy(creator)
                .build();
        return mapToResponse(examRepository.save(exam));
    }

    @Transactional
    public ExamResponse updateExamStatus(UUID id, ExamStatus status) {
        Exam exam = findById(id);
        exam.setStatus(status);
        return mapToResponse(examRepository.save(exam));
    }

    @Transactional
    public void deleteExam(UUID id) {
        if (!examRepository.existsById(id)) throw new ResourceNotFoundException("Exam", "id", id);
        examRepository.deleteById(id);
    }

    // Exam Results
    public List<ExamResultResponse> getResultsByExam(UUID examId) {
        return resultRepository.findByExamId(examId).stream().map(this::mapResultToResponse).collect(Collectors.toList());
    }

    public List<ExamResultResponse> getResultsByStudent(UUID studentId) {
        return resultRepository.findByStudentId(studentId).stream().map(this::mapResultToResponse).collect(Collectors.toList());
    }

    public List<ExamResultResponse> getStudentResultsByExam(UUID examId, UUID studentId) {
        return resultRepository.findByStudentIdAndExamId(studentId, examId)
                .stream().map(this::mapResultToResponse).collect(Collectors.toList());
    }

    @Transactional
    public ExamResultResponse addExamResult(UUID examId, UUID userId, CreateExamResultRequest request) {
        Exam exam = findById(examId);
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", request.getStudentId()));
        Teacher teacher = teacherRepository.findByUserId(userId)
                .orElseGet(() -> teacherRepository.findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("Teacher", "userId", userId)));

        resultRepository.findByExamIdAndStudentIdAndSubject(examId, request.getStudentId(), request.getSubject())
                .ifPresent(r -> { throw new ConflictException("Result already exists for this exam, student and subject"); });

        ExamResult result = ExamResult.builder()
                .exam(exam).student(student).subject(request.getSubject())
                .maxMarks(request.getMaxMarks()).obtainedMarks(request.getObtainedMarks())
                .grade(request.getGrade()).remarks(request.getRemarks())
                .examDate(request.getExamDate()).enteredBy(teacher)
                .build();
        return mapResultToResponse(resultRepository.save(result));
    }

    @Transactional
    public ExamResultResponse updateExamResult(UUID resultId, CreateExamResultRequest request) {
        ExamResult result = resultRepository.findById(resultId)
                .orElseThrow(() -> new ResourceNotFoundException("ExamResult", "id", resultId));
        result.setObtainedMarks(request.getObtainedMarks());
        result.setGrade(request.getGrade());
        result.setRemarks(request.getRemarks());
        return mapResultToResponse(resultRepository.save(result));
    }

    private Exam findById(UUID id) {
        return examRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exam", "id", id));
    }

    private ExamResponse mapToResponse(Exam e) {
        return ExamResponse.builder()
                .id(e.getId()).name(e.getName()).className(e.getClassName())
                .section(e.getSection()).academicYear(e.getAcademicYear())
                .startDate(e.getStartDate()).endDate(e.getEndDate())
                .status(e.getStatus()).createdAt(e.getCreatedAt())
                .build();
    }

    private ExamResultResponse mapResultToResponse(ExamResult r) {
        double percentage = r.getMaxMarks() > 0 ? (double) r.getObtainedMarks() / r.getMaxMarks() * 100 : 0;
        return ExamResultResponse.builder()
                .id(r.getId()).examId(r.getExam().getId()).examName(r.getExam().getName())
                .studentId(r.getStudent().getId()).studentName(r.getStudent().getUser().getName())
                .subject(r.getSubject()).maxMarks(r.getMaxMarks()).obtainedMarks(r.getObtainedMarks())
                .grade(r.getGrade()).percentage(Math.round(percentage * 100.0) / 100.0)
                .remarks(r.getRemarks()).examDate(r.getExamDate()).createdAt(r.getCreatedAt())
                .build();
    }
}

package com.school.service.impl;

import com.school.dto.request.AttendanceRequest;
import com.school.dto.request.BulkAttendanceRequest;
import com.school.dto.response.AttendanceResponse;
import com.school.dto.response.AttendanceSummaryResponse;
import com.school.entity.AttendanceRecord;
import com.school.entity.Student;
import com.school.entity.Teacher;
import com.school.enums.AttendanceStatus;
import com.school.exception.BadRequestException;
import com.school.exception.ResourceNotFoundException;
import com.school.repository.AttendanceRepository;
import com.school.repository.StudentRepository;
import com.school.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;

    public List<AttendanceResponse> getStudentAttendance(UUID studentId, LocalDate from, LocalDate to) {
        List<AttendanceRecord> records = (from != null && to != null)
                ? attendanceRepository.findByStudentIdAndDateBetween(studentId, from, to)
                : attendanceRepository.findByStudentId(studentId);
        return records.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<AttendanceResponse> getClassAttendanceByDate(String className, String section, LocalDate date) {
        return attendanceRepository.findByClassNameAndSectionAndDate(className, section, date)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public AttendanceSummaryResponse getStudentAttendanceSummary(UUID studentId, LocalDate from, LocalDate to) {
        List<AttendanceRecord> records = attendanceRepository.findByStudentIdAndDateBetween(studentId, from, to);
        long total = records.size();
        long present = records.stream().filter(r -> r.getStatus() == AttendanceStatus.present).count();
        long absent = records.stream().filter(r -> r.getStatus() == AttendanceStatus.absent).count();
        long late = records.stream().filter(r -> r.getStatus() == AttendanceStatus.late).count();
        double percentage = total > 0 ? (double) present / total * 100 : 0;
        return AttendanceSummaryResponse.builder()
                .totalDays(total).presentDays(present).absentDays(absent)
                .lateDays(late).attendancePercentage(Math.round(percentage * 100.0) / 100.0)
                .build();
    }

    @Transactional
    public AttendanceResponse markAttendance(UUID userId, AttendanceRequest request) {
        Teacher teacher = teacherRepository.findByUserId(userId)
                .orElseGet(() -> teacherRepository.findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("Teacher", "userId", userId)));
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", request.getStudentId()));

        LocalDate date = request.getDate() != null ? request.getDate() : LocalDate.now();
        if (attendanceRepository.existsByStudentIdAndDateAndSubject(request.getStudentId(), date, request.getSubject())) {
            throw new BadRequestException("Attendance already marked for this student on " + date);
        }

        AttendanceRecord record = AttendanceRecord.builder()
                .student(student).teacher(teacher)
                .className(student.getClassName()).section(student.getSection())
                .subject(request.getSubject()).date(date)
                .status(request.getStatus()).remarks(request.getRemarks())
                .build();
        return mapToResponse(attendanceRepository.save(record));
    }

    @Transactional
    public List<AttendanceResponse> markBulkAttendance(UUID userId, BulkAttendanceRequest request) {
        Teacher teacher = teacherRepository.findByUserId(userId)
                .orElseGet(() -> teacherRepository.findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("Teacher", "userId", userId)));
        LocalDate date = request.getDate() != null ? request.getDate() : LocalDate.now();

        return request.getRecords().stream().map(rec -> {
            Student student = studentRepository.findById(rec.getStudentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Student", "id", rec.getStudentId()));
            if (attendanceRepository.existsByStudentIdAndDateAndSubject(rec.getStudentId(), date, request.getSubject())) {
                throw new BadRequestException("Attendance already marked for student: " + rec.getStudentId());
            }
            AttendanceRecord record = AttendanceRecord.builder()
                    .student(student).teacher(teacher)
                    .className(request.getClassName()).section(request.getSection())
                    .subject(request.getSubject()).date(date)
                    .status(rec.getStatus()).remarks(rec.getRemarks())
                    .build();
            return mapToResponse(attendanceRepository.save(record));
        }).collect(Collectors.toList());
    }

    @Transactional
    public AttendanceResponse updateAttendance(UUID id, AttendanceStatus status, String remarks) {
        AttendanceRecord record = attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AttendanceRecord", "id", id));
        record.setStatus(status);
        if (remarks != null) record.setRemarks(remarks);
        return mapToResponse(attendanceRepository.save(record));
    }

    @Transactional
    public void deleteAttendance(UUID id) {
        if (!attendanceRepository.existsById(id)) throw new ResourceNotFoundException("AttendanceRecord", "id", id);
        attendanceRepository.deleteById(id);
    }

    private AttendanceResponse mapToResponse(AttendanceRecord r) {
        return AttendanceResponse.builder()
                .id(r.getId()).studentId(r.getStudent().getId())
                .studentName(r.getStudent().getUser().getName())
                .rollNumber(r.getStudent().getRollNumber())
                .teacherId(r.getTeacher().getId())
                .className(r.getClassName()).section(r.getSection())
                .subject(r.getSubject()).date(r.getDate())
                .status(r.getStatus()).remarks(r.getRemarks())
                .markedAt(r.getMarkedAt())
                .build();
    }
}

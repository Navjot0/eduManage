package com.school.service.impl;

import com.school.dto.request.AttendanceRequest;
import com.school.dto.request.BulkAttendanceRequest;
import com.school.dto.response.AttendanceResponse;
import com.school.dto.response.AttendanceSummaryResponse;
import com.school.dto.response.ClassAttendanceResponse;
import com.school.dto.response.ClassAttendanceResponse.StudentAttendanceEntry;
import com.school.dto.response.ClassAttendanceSummaryResponse;
import com.school.dto.response.ClassAttendanceSummaryResponse.StudentSummaryEntry;
import com.school.dto.response.ClassAttendanceSummaryResponse.DailySummaryEntry;
import com.school.entity.AttendanceRecord;
import com.school.entity.Student;
import com.school.entity.Teacher;
import com.school.enums.AttendanceStatus;
import com.school.exception.BadRequestException;
import com.school.exception.ResourceNotFoundException;
import com.school.repository.AttendanceRepository;
import com.school.repository.ClassRepository;
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
    private final ClassRepository classRepository;

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
                .orElseThrow(() -> new ResourceNotFoundException("Teacher profile not found for userId", "userId", userId));
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
                .orElseThrow(() -> new ResourceNotFoundException("Teacher profile not found for userId", "userId", userId));
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

    // ── Admin: get attendance by class ──────────────────────────────────────

    /**
     * GET /attendance/classes/{className}/{section}?date=
     * All students in the class for a specific date — shows "not marked" if absent from records.
     */
    public ClassAttendanceResponse getClassAttendanceForDate(
            String className, String section, String subject, LocalDate date) {

        // Fetch all active students in the class
        List<com.school.entity.Student> students =
                studentRepository.findByClassNameAndSectionAndStatus(
                        className, section, com.school.enums.StatusActive.active);

        // Fetch attendance records for that date
        List<AttendanceRecord> records = subject != null
                ? attendanceRepository.findByClassNameAndSectionAndSubjectAndDate(
                className, section, subject, date)
                : attendanceRepository.findByClassNameAndSectionAndDate(className, section, date);

        // Map studentId → record for quick lookup
        java.util.Map<UUID, AttendanceRecord> recordMap = records.stream()
                .collect(java.util.stream.Collectors.toMap(
                        r -> r.getStudent().getId(),
                        r -> r,
                        (a, b) -> a  // keep first if duplicate
                ));

        List<StudentAttendanceEntry> entries = students.stream()
                .sorted(java.util.Comparator.comparing(com.school.entity.Student::getRollNumber))
                .map(s -> {
                    AttendanceRecord rec = recordMap.get(s.getId());
                    return StudentAttendanceEntry.builder()
                            .studentId(s.getId())
                            .studentName(s.getUser().getName())
                            .rollNumber(s.getRollNumber())
                            .status(rec != null ? rec.getStatus() : null)
                            .remarks(rec != null ? rec.getRemarks() : null)
                            .date(date)
                            .build();
                })
                .collect(java.util.stream.Collectors.toList());

        long present    = entries.stream().filter(e -> e.getStatus() == AttendanceStatus.present).count();
        long absent     = entries.stream().filter(e -> e.getStatus() == AttendanceStatus.absent).count();
        long late       = entries.stream().filter(e -> e.getStatus() == AttendanceStatus.late).count();
        long notMarked  = entries.stream().filter(e -> e.getStatus() == null).count();
        long marked     = present + absent + late;
        double pct      = marked > 0 ? (double) present / marked * 100 : 0;

        return ClassAttendanceResponse.builder()
                .className(className).section(section).subject(subject).date(date)
                .totalStudents(students.size())
                .presentCount((int) present).absentCount((int) absent)
                .lateCount((int) late).notMarkedCount((int) notMarked)
                .attendancePercent(Math.round(pct * 100.0) / 100.0)
                .students(entries)
                .build();
    }

    /**
     * GET /attendance/classes/{className}/{section}/summary?from=&to=
     * Full summary for a class over a date range — per-student and daily breakdown.
     */
    public ClassAttendanceSummaryResponse getClassAttendanceSummary(
            String className, String section, LocalDate from, LocalDate to) {

        List<com.school.entity.Student> students =
                studentRepository.findByClassNameAndSectionAndStatus(
                        className, section, com.school.enums.StatusActive.active);

        List<AttendanceRecord> allRecords =
                attendanceRepository.findByClassNameAndSectionAndDateBetween(
                        className, section, from, to);

        // Group records by studentId
        java.util.Map<UUID, List<AttendanceRecord>> byStudent = allRecords.stream()
                .collect(java.util.stream.Collectors.groupingBy(r -> r.getStudent().getId()));

        List<StudentSummaryEntry> studentSummaries = students.stream()
                .sorted(java.util.Comparator.comparing(com.school.entity.Student::getRollNumber))
                .map(s -> {
                    List<AttendanceRecord> recs = byStudent.getOrDefault(s.getId(), java.util.List.of());
                    long present = recs.stream().filter(r -> r.getStatus() == AttendanceStatus.present).count();
                    long absent  = recs.stream().filter(r -> r.getStatus() == AttendanceStatus.absent).count();
                    long late    = recs.stream().filter(r -> r.getStatus() == AttendanceStatus.late).count();
                    long total   = recs.size();
                    double pct   = total > 0 ? (double) present / total * 100 : 0;
                    return StudentSummaryEntry.builder()
                            .studentId(s.getId()).studentName(s.getUser().getName())
                            .rollNumber(s.getRollNumber())
                            .presentDays(present).absentDays(absent).lateDays(late)
                            .totalMarkedDays(total)
                            .attendancePercent(Math.round(pct * 100.0) / 100.0)
                            .build();
                })
                .collect(java.util.stream.Collectors.toList());

        // Group records by date for daily summary
        java.util.Map<java.time.LocalDate, List<AttendanceRecord>> byDate = allRecords.stream()
                .collect(java.util.stream.Collectors.groupingBy(AttendanceRecord::getDate));

        List<DailySummaryEntry> dailySummary = byDate.entrySet().stream()
                .sorted(java.util.Map.Entry.comparingByKey())
                .map(entry -> {
                    List<AttendanceRecord> recs = entry.getValue();
                    long present = recs.stream().filter(r -> r.getStatus() == AttendanceStatus.present).count();
                    long absent  = recs.stream().filter(r -> r.getStatus() == AttendanceStatus.absent).count();
                    long late    = recs.stream().filter(r -> r.getStatus() == AttendanceStatus.late).count();
                    long total   = recs.size();
                    double pct   = total > 0 ? (double) present / total * 100 : 0;
                    return DailySummaryEntry.builder()
                            .date(entry.getKey())
                            .presentCount((int) present).absentCount((int) absent)
                            .lateCount((int) late).totalMarked((int) total)
                            .attendancePercent(Math.round(pct * 100.0) / 100.0)
                            .build();
                })
                .collect(java.util.stream.Collectors.toList());

        // Class-level totals
        long totalPresent = allRecords.stream().filter(r -> r.getStatus() == AttendanceStatus.present).count();
        long totalAbsent  = allRecords.stream().filter(r -> r.getStatus() == AttendanceStatus.absent).count();
        long totalLate    = allRecords.stream().filter(r -> r.getStatus() == AttendanceStatus.late).count();
        double classPct   = allRecords.size() > 0
                ? (double) totalPresent / allRecords.size() * 100 : 0;

        return ClassAttendanceSummaryResponse.builder()
                .className(className).section(section).from(from).to(to)
                .totalStudents(students.size())
                .students(studentSummaries)
                .dailySummary(dailySummary)
                .totalRecords((long) allRecords.size())
                .totalPresent(totalPresent).totalAbsent(totalAbsent).totalLate(totalLate)
                .classAttendancePercent(Math.round(classPct * 100.0) / 100.0)
                .build();
    }

    /**
     * GET /attendance/classes/{className}/{section}/range?from=&to=
     * All raw attendance records for a class over a date range.
     */
    public List<AttendanceResponse> getClassAttendanceRange(
            String className, String section, LocalDate from, LocalDate to, String subject) {
        List<AttendanceRecord> records = subject != null
                ? attendanceRepository.findByClassNameAndSectionAndSubjectAndDateBetween(
                className, section, subject, from, to)
                : attendanceRepository.findByClassNameAndSectionAndDateBetween(
                className, section, from, to);
        return records.stream()
                .sorted(java.util.Comparator.comparing(AttendanceRecord::getDate)
                        .thenComparing(r -> r.getStudent().getRollNumber()))
                .map(this::mapToResponse)
                .collect(java.util.stream.Collectors.toList());
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
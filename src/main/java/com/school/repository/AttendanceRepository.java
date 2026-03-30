package com.school.repository;

import com.school.entity.AttendanceRecord;
import com.school.enums.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface AttendanceRepository extends JpaRepository<AttendanceRecord, UUID> {
    List<AttendanceRecord> findByStudentIdAndDateBetween(UUID studentId, LocalDate from, LocalDate to);
    List<AttendanceRecord> findByClassNameAndSectionAndDate(String className, String section, LocalDate date);
    List<AttendanceRecord> findByStudentId(UUID studentId);

    @Query("SELECT COUNT(a) FROM AttendanceRecord a WHERE a.student.id = :studentId AND a.status = :status AND a.date BETWEEN :from AND :to")
    Long countByStudentIdAndStatusBetween(@Param("studentId") UUID studentId,
                                          @Param("status") AttendanceStatus status,
                                          @Param("from") LocalDate from,
                                          @Param("to") LocalDate to);

    boolean existsByStudentIdAndDateAndSubject(UUID studentId, LocalDate date, String subject);

    @org.springframework.data.jpa.repository.Query(
            "SELECT COUNT(a) FROM AttendanceRecord a WHERE a.student.id = :studentId AND a.date BETWEEN :from AND :to")
    long countTotalByStudentAndDateRange(
            @org.springframework.data.repository.query.Param("studentId") UUID studentId,
            @org.springframework.data.repository.query.Param("from") java.time.LocalDate from,
            @org.springframework.data.repository.query.Param("to") java.time.LocalDate to);

    @org.springframework.data.jpa.repository.Query(
            "SELECT COUNT(a) FROM AttendanceRecord a WHERE a.student.id = :studentId AND a.status = 'present' AND a.date BETWEEN :from AND :to")
    long countPresentByStudentAndDateRange(
            @org.springframework.data.repository.query.Param("studentId") UUID studentId,
            @org.springframework.data.repository.query.Param("from") java.time.LocalDate from,
            @org.springframework.data.repository.query.Param("to") java.time.LocalDate to);

    // ── Admin: class-level attendance queries ─────────────────────────────

    /** All records for a class within a date range */
    List<AttendanceRecord> findByClassNameAndSectionAndDateBetween(
            String className, String section,
            LocalDate from, LocalDate to);

    /** All records for a class within a date range, filtered by subject */
    List<AttendanceRecord> findByClassNameAndSectionAndSubjectAndDateBetween(
            String className, String section, String subject,
            LocalDate from, LocalDate to);

    /** All records for a class on a specific date, optionally by subject */
    List<AttendanceRecord> findByClassNameAndSectionAndSubjectAndDate(
            String className, String section, String subject, LocalDate date);

    /** Get all unique dates attendance was marked for a class */
    @org.springframework.data.jpa.repository.Query(
            "SELECT DISTINCT a.date FROM AttendanceRecord a " +
                    "WHERE a.className = :className AND a.section = :section " +
                    "AND a.date BETWEEN :from AND :to ORDER BY a.date")
    List<LocalDate> findDistinctDatesByClassAndDateRange(
            @org.springframework.data.repository.query.Param("className") String className,
            @org.springframework.data.repository.query.Param("section") String section,
            @org.springframework.data.repository.query.Param("from") LocalDate from,
            @org.springframework.data.repository.query.Param("to") LocalDate to);

    /** Count by status for a class on a date */
    @org.springframework.data.jpa.repository.Query(
            "SELECT a.status, COUNT(a) FROM AttendanceRecord a " +
                    "WHERE a.className = :className AND a.section = :section AND a.date = :date " +
                    "GROUP BY a.status")
    List<Object[]> countByStatusForClassOnDate(
            @org.springframework.data.repository.query.Param("className") String className,
            @org.springframework.data.repository.query.Param("section") String section,
            @org.springframework.data.repository.query.Param("date") LocalDate date);

    /** Count by status for a class within a date range */
    @org.springframework.data.jpa.repository.Query(
            "SELECT a.status, COUNT(a) FROM AttendanceRecord a " +
                    "WHERE a.className = :className AND a.section = :section " +
                    "AND a.date BETWEEN :from AND :to GROUP BY a.status")
    List<Object[]> countByStatusForClassInRange(
            @org.springframework.data.repository.query.Param("className") String className,
            @org.springframework.data.repository.query.Param("section") String section,
            @org.springframework.data.repository.query.Param("from") LocalDate from,
            @org.springframework.data.repository.query.Param("to") LocalDate to);
}
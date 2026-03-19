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
}

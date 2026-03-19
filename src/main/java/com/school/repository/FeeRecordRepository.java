package com.school.repository;

import com.school.entity.FeeRecord;
import com.school.enums.FeeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FeeRecordRepository extends JpaRepository<FeeRecord, UUID> {
    List<FeeRecord> findByStudentId(UUID studentId);
    List<FeeRecord> findByStudentIdAndStatus(UUID studentId, FeeStatus status);
    List<FeeRecord> findByStudentIdAndAcademicYear(UUID studentId, String academicYear);
    List<FeeRecord> findByStatus(FeeStatus status);
    boolean existsByReceiptNumber(String receiptNumber);
}

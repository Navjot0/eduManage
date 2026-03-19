package com.school.service.impl;

import com.school.dto.request.CreateFeeRecordRequest;
import com.school.dto.request.CreateFeeTypeRequest;
import com.school.dto.request.UpdateFeeRecordRequest;
import com.school.dto.response.FeeRecordResponse;
import com.school.dto.response.FeeTypeResponse;
import com.school.entity.FeeRecord;
import com.school.entity.FeeType;
import com.school.entity.Student;
import com.school.enums.FeeStatus;
import com.school.exception.BadRequestException;
import com.school.exception.ConflictException;
import com.school.exception.ResourceNotFoundException;
import com.school.repository.FeeRecordRepository;
import com.school.repository.FeeTypeRepository;
import com.school.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeeService {

    private final FeeTypeRepository feeTypeRepository;
    private final FeeRecordRepository feeRecordRepository;
    private final StudentRepository studentRepository;

    // Fee Types
    public List<FeeTypeResponse> getAllFeeTypes() {
        return feeTypeRepository.findAll().stream().map(this::mapTypeToResponse).collect(Collectors.toList());
    }

    public List<FeeTypeResponse> getActiveFeeTypes() {
        return feeTypeRepository.findByIsActive(true).stream().map(this::mapTypeToResponse).collect(Collectors.toList());
    }

    @Transactional
    public FeeTypeResponse createFeeType(CreateFeeTypeRequest request) {
        if (feeTypeRepository.existsByName(request.getName()))
            throw new ConflictException("Fee type already exists: " + request.getName());
        FeeType feeType = FeeType.builder()
                .name(request.getName()).description(request.getDescription()).isActive(true).build();
        return mapTypeToResponse(feeTypeRepository.save(feeType));
    }

    @Transactional
    public FeeTypeResponse toggleFeeType(UUID id) {
        FeeType feeType = feeTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FeeType", "id", id));
        feeType.setIsActive(!feeType.getIsActive());
        return mapTypeToResponse(feeTypeRepository.save(feeType));
    }

    // Fee Records
    public List<FeeRecordResponse> getFeesByStudent(UUID studentId) {
        return feeRecordRepository.findByStudentId(studentId)
                .stream().map(this::mapRecordToResponse).collect(Collectors.toList());
    }

    public List<FeeRecordResponse> getFeesByStudentAndYear(UUID studentId, String academicYear) {
        return feeRecordRepository.findByStudentIdAndAcademicYear(studentId, academicYear)
                .stream().map(this::mapRecordToResponse).collect(Collectors.toList());
    }

    public List<FeeRecordResponse> getFeesByStatus(FeeStatus status) {
        return feeRecordRepository.findByStatus(status)
                .stream().map(this::mapRecordToResponse).collect(Collectors.toList());
    }

    public FeeRecordResponse getFeeById(UUID id) {
        return mapRecordToResponse(findRecordById(id));
    }

    @Transactional
    public FeeRecordResponse createFeeRecord(CreateFeeRecordRequest request) {
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", request.getStudentId()));
        FeeRecord record = FeeRecord.builder()
                .student(student).feeType(request.getFeeType()).amount(request.getAmount())
                .paidAmount(BigDecimal.ZERO).status(request.getStatus()).dueDate(request.getDueDate())
                .academicYear(request.getAcademicYear()).quarter(request.getQuarter())
                .remarks(request.getRemarks())
                .build();
        return mapRecordToResponse(feeRecordRepository.save(record));
    }

    @Transactional
    public FeeRecordResponse updateFeeRecord(UUID id, UpdateFeeRecordRequest request) {
        FeeRecord record = findRecordById(id);
        if (request.getPaidAmount() != null) {
            if (request.getPaidAmount().compareTo(record.getAmount()) > 0)
                throw new BadRequestException("Paid amount cannot exceed total amount");
            record.setPaidAmount(request.getPaidAmount());
        }
        if (request.getStatus() != null) record.setStatus(request.getStatus());
        if (request.getPaidDate() != null) record.setPaidDate(request.getPaidDate());
        if (request.getReceiptNumber() != null) {
            if (feeRecordRepository.existsByReceiptNumber(request.getReceiptNumber()))
                throw new ConflictException("Receipt number already used: " + request.getReceiptNumber());
            record.setReceiptNumber(request.getReceiptNumber());
        }
        if (request.getRemarks() != null) record.setRemarks(request.getRemarks());
        return mapRecordToResponse(feeRecordRepository.save(record));
    }

    private FeeRecord findRecordById(UUID id) {
        return feeRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FeeRecord", "id", id));
    }

    private FeeTypeResponse mapTypeToResponse(FeeType f) {
        return FeeTypeResponse.builder()
                .id(f.getId()).name(f.getName()).description(f.getDescription())
                .isActive(f.getIsActive()).createdAt(f.getCreatedAt())
                .build();
    }

    private FeeRecordResponse mapRecordToResponse(FeeRecord r) {
        BigDecimal balance = r.getAmount().subtract(r.getPaidAmount());
        return FeeRecordResponse.builder()
                .id(r.getId()).studentId(r.getStudent().getId())
                .studentName(r.getStudent().getUser().getName())
                .rollNumber(r.getStudent().getRollNumber())
                .feeType(r.getFeeType()).amount(r.getAmount()).paidAmount(r.getPaidAmount())
                .balanceAmount(balance).status(r.getStatus())
                .dueDate(r.getDueDate()).paidDate(r.getPaidDate())
                .receiptNumber(r.getReceiptNumber()).academicYear(r.getAcademicYear())
                .quarter(r.getQuarter()).remarks(r.getRemarks()).createdAt(r.getCreatedAt())
                .build();
    }
}

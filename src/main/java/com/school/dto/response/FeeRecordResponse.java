package com.school.dto.response;
import com.school.enums.FeeStatus;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder
public class FeeRecordResponse {
    private UUID id;
    private UUID studentId;
    private String studentName;
    private String rollNumber;
    private String feeType;
    private BigDecimal amount;
    private BigDecimal paidAmount;
    private BigDecimal balanceAmount;
    private FeeStatus status;
    private LocalDate dueDate;
    private LocalDate paidDate;
    private String receiptNumber;
    private String academicYear;
    private String quarter;
    private String remarks;
    private LocalDateTime createdAt;
}

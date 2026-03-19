package com.school.dto.request;
import com.school.enums.FeeStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UpdateFeeRecordRequest {
    private BigDecimal paidAmount;
    private FeeStatus status;
    private LocalDate paidDate;
    private String receiptNumber;
    private String remarks;
}

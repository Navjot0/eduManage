package com.school.dto.request;
import com.school.enums.FeeStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class CreateFeeRecordRequest {
    @NotNull private UUID studentId;
    @NotNull private String feeType;
    @NotNull private BigDecimal amount;
    @NotNull private FeeStatus status;
    @NotNull private LocalDate dueDate;
    @NotNull private String academicYear;
    private String quarter;
    private String remarks;
}

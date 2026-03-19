package com.school.dto.request;
import com.school.enums.StatusActive;
import lombok.Data;

@Data
public class UpdateTeacherRequest {
    private String subject;
    private String department;
    private String qualification;
    private StatusActive status;
}

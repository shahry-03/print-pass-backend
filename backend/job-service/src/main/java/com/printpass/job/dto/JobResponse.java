package com.printpass.job.dto;
import com.printpass.job.enums.JobStatus;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
@Data
@Builder
public class JobResponse {
    private Long id;
    private Long studentId;
    private Long shopId;
    private String documentName;
    private Integer totalPages;
    private BigDecimal totalCost; // Yeh hum khud calculate karenge!
    private JobStatus status;
    // Baki fields ke sath isay bhi add karein
    private String pin;
}
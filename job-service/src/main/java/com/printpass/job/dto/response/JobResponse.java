package com.printpass.job.dto.response;

import com.printpass.job.enums.JobStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobResponse {
    private UUID id;
    private String secretKey;       // ← Student ko milega
    private String fileName;
    private JobStatus jobStatus;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    // ⚠️ fileUrl response mein nahi! Security reason
    // fileUrl sirf print karte waqt milega
}
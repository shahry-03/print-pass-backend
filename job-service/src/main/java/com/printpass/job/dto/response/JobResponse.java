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
    private String secretKey;       // ← Student will get
    private String fileName;
    private JobStatus jobStatus;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    
    // New FIELDS
    private int totalPages;            // Pages count (0 agar abhi convert nahi hua)
    private String conversionStatus;   // PENDING, SUCCESS, FAILED
    private String convertedPdfUrl;    // (Optional) Agar frontend ko direct PDF link chahiye
    
    // ⚠️ fileUrl is not in response due to Security reason
    // fileUrl sirf print karte waqt milega
}
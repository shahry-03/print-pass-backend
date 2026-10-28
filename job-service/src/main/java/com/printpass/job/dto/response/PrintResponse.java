// job-service/src/main/java/com/printpass/job/dto/response/PrintResponse.java
package com.printpass.job.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PrintResponse {
    private String fileName;
    private String fileUrl;          // Original file URL
    private String convertedPdfUrl;  // New PDF URL
    private int totalPages;          // NewPage Count
    private String message;
}
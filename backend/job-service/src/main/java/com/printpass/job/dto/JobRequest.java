package com.printpass.job.dto;
import lombok.Data;
@Data
public class JobRequest {
    private Long studentId;
    private Long shopId;
    private String documentName;
    private Integer totalPages;
    private boolean isColor; // Abhi hum basic rakh rahe hain, color ki logic baad mein add kar lenge
}
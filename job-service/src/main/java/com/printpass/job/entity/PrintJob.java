package com.printpass.job.entity;

import com.printpass.job.enums.JobStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "print_jobs",
        indexes = {
                @Index(name = "idx_secret_key", columnList = "secret_key"),
                @Index(name = "idx_shop_id", columnList = "shop_id"),
                @Index(name = "idx_job_status", columnList = "job_status"),
                @Index(name = "idx_expires_at", columnList = "expires_at")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrintJob {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "secret_key", nullable = false, unique = true)
    private String secretKey;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_url", nullable = false)
    private String fileUrl;

    // New FIELD: Converted PDF URL
    @Column(name = "converted_pdf_url")
    private String convertedPdfUrl;

    // New FIELD: total pages of PDF
    @Column(name = "total_pages")
    private int totalPages = 0;

    // New FIELD: Conversion status (PENDING, SUCCESS, FAILED)
    @Column(name = "conversion_status")
    private String conversionStatus = "PENDING";

    @Enumerated(EnumType.STRING)
    @Column(name = "job_status", nullable = false)
    private JobStatus jobStatus;

    @Column(name = "shop_id")
    private UUID shopId;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        jobStatus = JobStatus.PENDING;
        expiresAt = LocalDateTime.now().plusHours(24);
    }
}
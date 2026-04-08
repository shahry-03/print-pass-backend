package com.printpass.job.entity;

import com.printpass.job.enums.JobStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "print_jobs",
        indexes = {
                // PIN se file dhundhne ke liye — Sabse zyada use hoga!
                @Index(name = "idx_secret_key", columnList = "secret_key"),
                // Shop ID se jobs dhundhne ke liye
                @Index(name = "idx_shop_id", columnList = "shop_id"),
                // Status se jobs filter karne ke liye
                @Index(name = "idx_job_status", columnList = "job_status"),
                // Expire hone wali jobs dhundhne ke liye
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

    // ✅ 6-digit secret PIN — Unique hona chahiye!
    @Column(name = "secret_key", nullable = false, unique = true)
    private String secretKey;

    // ✅ Original file ka naam (e.g., "AI_Notes.pdf")
    @Column(name = "file_name", nullable = false)
    private String fileName;

    // ✅ DigitalOcean Spaces ka file URL
    @Column(name = "file_url", nullable = false)
    private String fileUrl;

    // ✅ Job ka current status
    @Enumerated(EnumType.STRING)
    @Column(name = "job_status", nullable = false)
    private JobStatus jobStatus;

    // ✅ Kaun si shop ne print kiya
    // User Service se verify hoga
    @Column(name = "shop_id")
    private UUID shopId;

    // ✅ PIN kab expire hoga (24 hours)
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    // ✅ Job kab bani
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        jobStatus = JobStatus.PENDING;    // ← Default status
        expiresAt = LocalDateTime.now().plusHours(24); // ← 24 hours valid
    }
}
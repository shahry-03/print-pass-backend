package com.printpass.job.repository;

import com.printpass.job.entity.PrintJob;
import com.printpass.job.enums.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PrintJobRepository extends JpaRepository<PrintJob, UUID> {

    // ✅ 1. PIN se job dhundhna — Sabse important!
    // Student PIN deta hai → Shop wala enter karta hai → File aati hai
    Optional<PrintJob> findBySecretKey(String secretKey);

    // ✅ 2. PIN aur Status se job dhundhna
    // Sirf PENDING jobs milegi — Expire ya Complete nahi
    Optional<PrintJob> findBySecretKeyAndJobStatus(String secretKey, JobStatus jobStatus);

    // ✅ 3. Shop ki saari jobs
    // Shop wala apni saari jobs dekhe
    List<PrintJob> findByShopId(UUID shopId);

    // ✅ 4. Shop ki jobs status se filter
    // Shop wala sirf PENDING jobs dekhe
    List<PrintJob> findByShopIdAndJobStatus(UUID shopId, JobStatus jobStatus);

    // ✅ 5. Expire ho chuki jobs dhundhna
    // Cleanup ke liye — 24 ghante baad automatically delete
    List<PrintJob> findByExpiresAtBeforeAndJobStatus(
            LocalDateTime dateTime,
            JobStatus jobStatus
    );

    // ✅ 6. PIN already exist karta hai?
    // Duplicate PIN check ke liye
    boolean existsBySecretKey(String secretKey);

    // ✅ 7. Expire jobs bulk delete
    // Scheduled cleanup ke liye
    @Modifying
    @Query("DELETE FROM PrintJob p WHERE p.expiresAt < :dateTime AND p.jobStatus = :status")
    void deleteExpiredJobs(LocalDateTime dateTime, JobStatus status);
}
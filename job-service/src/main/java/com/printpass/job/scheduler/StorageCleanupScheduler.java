package com.printpass.job.scheduler;

import com.printpass.job.entity.PrintJob;
import com.printpass.job.enums.JobStatus;
import com.printpass.job.repository.PrintJobRepository;
import com.printpass.job.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class StorageCleanupScheduler {

    private final PrintJobRepository printJobRepository;
    private final StorageService storageService;

    // Yeh task har raat 2 baje chalega
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupExpiredFiles() {
        log.info("🧹 Starting daily storage cleanup task...");
        
        // Aaj se 24 ghante pehle ka time
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(24);

        // Woh jobs dhoondo jo expire ho chuki hain ya complete ho chuki hain
        List<PrintJob> expiredJobs = printJobRepository.findByExpiresAtBeforeAndJobStatus(
                cutoffTime, JobStatus.PENDING); // Agar PENDING hai aur 24 ghante purani hai

        for (PrintJob job : expiredJobs) {
            try {
                // 1. MinIO se file delete karo
                if (job.getFileUrl() != null) {
                    storageService.deleteFile(job.getFileUrl());
                }
                // 2. Database se record delete karo
                printJobRepository.delete(job);
                log.info("✅ Deleted expired job ID: {}", job.getId());
            } catch (Exception e) {
                log.error("❌ Failed to delete job ID: {}", job.getId(), e);
            }
        }
        log.info("🧹 Cleanup task finished. Deleted {} files.", expiredJobs.size());
    }
}
package com.printpass.job.service.impl;

import com.printpass.job.dto.response.JobResponse;
import com.printpass.job.dto.response.PrintResponse;
import com.printpass.job.dto.response.ShopVerificationResponse;
import com.printpass.job.entity.PrintJob;
import com.printpass.job.enums.JobStatus;
import com.printpass.job.exception.InvalidPinException;
import com.printpass.job.exception.ShopNotVerifiedException;
import com.printpass.job.feign.UserServiceClient;
import com.printpass.job.repository.PrintJobRepository;
import com.printpass.job.service.JobService;
import com.printpass.job.storage.StorageService;
import com.printpass.job.utils.PinGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobServiceImpl implements JobService {

    private final PrintJobRepository printJobRepository;
    private final StorageService storageService;
    private final UserServiceClient userServiceClient;

    @Override
    @Transactional
    public JobResponse uploadFile(MultipartFile file) {
        log.debug("Uploading file: {}", file.getOriginalFilename());

        // 1. File ko DigitalOcean Spaces pe upload karo
        String fileUrl = storageService.uploadFile(file);
        String fileName = file.getOriginalFilename();

        // 2. 6-digit unique PIN generate karo
        String secretKey;
        do {
            secretKey = PinGenerator.generateSixDigitPin();
        } while (printJobRepository.existsBySecretKey(secretKey)); // unique hona chahiye

        // 3. Job entity banao
        PrintJob printJob = PrintJob.builder()
                .secretKey(secretKey)
                .fileName(fileName)
                .fileUrl(fileUrl)
                .jobStatus(JobStatus.PENDING)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();

        PrintJob savedJob = printJobRepository.save(printJob);

        // 4. Response return karo (PIN student ko milega)
        return JobResponse.builder()
                .id(savedJob.getId())
                .secretKey(savedJob.getSecretKey())
                .fileName(savedJob.getFileName())
                .jobStatus(savedJob.getJobStatus())
                .expiresAt(savedJob.getExpiresAt())
                .createdAt(savedJob.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public PrintResponse getFileByPin(String secretKey, UUID shopId) {
        log.debug("Fetching file for PIN: {}", secretKey);

        // 1. PIN se job dhundho
        PrintJob printJob = printJobRepository
                .findBySecretKeyAndJobStatus(secretKey, JobStatus.PENDING)
                .orElseThrow(() -> new InvalidPinException("Invalid or expired PIN: " + secretKey));

        // 2. PIN expire toh nahi hua?
        if (printJob.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidPinException("PIN has expired. Please upload again.");
        }

        // 3. Shop verified hai? User Service se check karo
        ShopVerificationResponse shop = userServiceClient.verifyShop(shopId);
        if (!shop.isVerified() || !shop.isActive()) {
            throw new ShopNotVerifiedException("Shop is not verified or inactive");
        }

        // 4. Shop ID assign karo
        printJob.setShopId(shop.getShopId());
        printJob.setJobStatus(JobStatus.PROCESSING); // Print start hone wala hai
        log.info("Job ID: {}, ShopId set to: {}", printJob.getId(), shop.getShopId());
        printJobRepository.save(printJob);

        // 5. Presigned URL generate karo (temporary)
        String presignedUrl = storageService.getPresignedUrl(printJob.getFileUrl());

        return PrintResponse.builder()
                .fileName(printJob.getFileName())
                .fileUrl(presignedUrl)
                .message("File ready for printing")
                .build();
    }

    @Override
    @Transactional
    public PrintResponse printJob(String secretKey, UUID userId) {
        // LOG ENTRY: Request aate hi check karein ke kisne call kiya hai
        log.info("Incoming print request - PIN: {}, Request User ID: {}", secretKey, userId);

        // 1. PIN se job dhundho
        PrintJob printJob = printJobRepository
                .findBySecretKeyAndJobStatus(secretKey, JobStatus.PROCESSING)
                .orElseThrow(() -> new InvalidPinException("Job not in PROCESSING state"));

        // 2. User Service se Shop ki ASLI ID mangwao
        ShopVerificationResponse shop = userServiceClient.verifyShop(userId);
        UUID actualShopId = shop.getShopId(); // DTO mein jo bhi naam hai (id ya shopId) wo lagayen

        // 🪵 LOGGING FIX 4 (The Debugger's Goldmine): Compare karne se pehle dono IDs print karein!
        log.info("Verification Check -> DB Stored ShopId: {}, Actual ShopId: {}", printJob.getShopId(), actualShopId);

        // 3. Verify shop
        if (printJob.getShopId() == null || !printJob.getShopId().equals(actualShopId)) {
            // 🪵 LOG ERROR: Agar clash ho jaye toh error ko bhi log karein
            log.warn("Security Alert: Unauthorized print attempt for PIN: {}", secretKey);

            throw new ShopNotVerifiedException(

                    String.format("Unauthorized: Job shopId=%s, Request shopId=%s",

                            printJob.getShopId(), actualShopId)

            );

        }

        // 3. Status update karo → READY (print nikal gaya)
        printJob.setJobStatus(JobStatus.READY);
        printJobRepository.save(printJob);

        // 🪵 LOGGING FIX 3 (The Victory Log): Database mein save hone ke baad success print karein
        log.info("Print Job {} status changed to READY by Shop {}", printJob.getId(), actualShopId);

        // 4. Response
        return PrintResponse.builder()
                .fileName(printJob.getFileName())
                .message("Print completed successfully! Ready for pickup.")
                .build();
    }
}
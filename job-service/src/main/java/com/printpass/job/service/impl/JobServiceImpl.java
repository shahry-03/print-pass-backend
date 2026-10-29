package com.printpass.job.service.impl;

import com.printpass.job.client.GotenbergClient;
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
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobServiceImpl implements JobService {

    private final PrintJobRepository printJobRepository;
    private final StorageService storageService;
    private final UserServiceClient userServiceClient;
    private final GotenbergClient gotenbergClient; // Gotenberg Inject kiya

    @Override
    @Transactional
    public JobResponse uploadFile(MultipartFile file) {
        log.debug("Uploading file: {}", file.getOriginalFilename());

        // 1. File ko Storage par upload karo
        String fileUrl = storageService.uploadFile(file);
        String fileName = file.getOriginalFilename();

        // 2. 6-digit unique PIN generate karo
        String secretKey;
        do {
            secretKey = PinGenerator.generateSixDigitPin();
        } while (printJobRepository.existsBySecretKey(secretKey));

        // 3. Job entity banao (Initial status PENDING)
        PrintJob printJob = PrintJob.builder()
                .secretKey(secretKey)
                .fileName(fileName)
                .fileUrl(fileUrl)
                .jobStatus(JobStatus.PENDING)
                .conversionStatus("PENDING")
                .totalPages(0)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();

        PrintJob savedJob = printJobRepository.save(printJob);

        // 4. Background Conversion Trigger Karein (CompletableFuture)
        try {
            byte[] fileBytes = file.getBytes();
            CompletableFuture.runAsync(() -> processBackgroundConversion(savedJob.getId(), fileBytes, fileName, file.getContentType()));
        } catch (Exception e) {
            log.error("Failed to initiate background conversion", e);
        }

        // 5. Response return karo (User ko turant PIN milega)
        return JobResponse.builder()
                .id(savedJob.getId())
                .secretKey(savedJob.getSecretKey())
                .fileName(savedJob.getFileName())
                .jobStatus(savedJob.getJobStatus())
                .conversionStatus(savedJob.getConversionStatus())
                .totalPages(savedJob.getTotalPages())
                .expiresAt(savedJob.getExpiresAt())
                .createdAt(savedJob.getCreatedAt())
                .build();
    }

    
    //SMART BACKGROUND WORKER METHOD
    private void processBackgroundConversion(UUID jobId, byte[] fileBytes, String fileName, String contentType) {
        try {
            log.info("Starting background processing for Job ID: {}, File: {}, Type: {}", jobId, fileName, contentType);

            String convertedPdfUrl;
            int totalPages;

            // 1. Check karein ke file pehle se PDF hai ya Image hai?
            boolean isPdf = contentType != null && contentType.equals("application/pdf");
            boolean isImage = contentType != null && contentType.startsWith("image/");

            if (isPdf || isImage) {
                // ✅ PDF ya Image hai, Gotenberg ki zaroorat nahi
                log.info("File is PDF/Image. Skipping Gotenberg conversion.");
                
                // Original file hi hamari final file hai
                PrintJob job = printJobRepository.findById(jobId).orElseThrow();
                convertedPdfUrl = job.getFileUrl(); // Original URL hi use kar lenge

                if (isPdf) {
                    // PDFBox se direct pages count karein
                    try (PDDocument document = PDDocument.load(fileBytes)) {
                        totalPages = document.getNumberOfPages();
                    }
                } else {
                    // Image ke liye hamesha 1 page
                    totalPages = 1;
                }

            } else {
                // 🔥 Word / PPT / Excel hai, Gotenberg ko bhejein
                log.info("File requires conversion (Word/PPT). Calling Gotenberg...");
                
                byte[] pdfBytes = gotenbergClient.convertToPdf(fileBytes, fileName);
                
                // Converted PDF ko storage par upload karein
                String pdfFileName = fileName.replaceAll("\\.[^.]+$", "") + "_converted.pdf";
                convertedPdfUrl = storageService.uploadBytes(pdfBytes, pdfFileName, "application/pdf");

                // PDFBox se pages count karein
                try (PDDocument document = PDDocument.load(pdfBytes)) {
                    totalPages = document.getNumberOfPages();
                }
            }

            // 2. Database mein update karein
            PrintJob job = printJobRepository.findById(jobId).orElseThrow();
            job.setConvertedPdfUrl(convertedPdfUrl);
            job.setTotalPages(totalPages);
            job.setConversionStatus("SUCCESS");
            printJobRepository.save(job);

            log.info("Background processing successful for Job ID: {}. Total Pages: {}", jobId, totalPages);

        } catch (Exception e) {
            log.error("Background processing failed for Job ID: {}", jobId, e);
            // Update status as FAILED
            printJobRepository.findById(jobId).ifPresent(job -> {
                job.setConversionStatus("FAILED");
                printJobRepository.save(job);
            });
        }
    }

    @Override
    @Transactional
    public JobResponse reprintJob(String oldPin) {
        log.debug("Reprinting job for old PIN: {}", oldPin);

        PrintJob oldJob = printJobRepository.findBySecretKey(oldPin)
                .orElseThrow(() -> new InvalidPinException("Invalid PIN: " + oldPin));

        String newSecretKey;
        do {
            newSecretKey = PinGenerator.generateSixDigitPin();
        } while (printJobRepository.existsBySecretKey(newSecretKey));

        // Purani job ki details (URL, Pages, Status) copy karein
        PrintJob newJob = PrintJob.builder()
                .secretKey(newSecretKey)
                .fileName(oldJob.getFileName())
                .fileUrl(oldJob.getFileUrl())
                .convertedPdfUrl(oldJob.getConvertedPdfUrl()) // PDF URL copy
                .totalPages(oldJob.getTotalPages())           // Pages copy
                .conversionStatus(oldJob.getConversionStatus()) // Status copy
                .jobStatus(JobStatus.PENDING)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();

        PrintJob savedJob = printJobRepository.save(newJob);

        return JobResponse.builder()
                .id(savedJob.getId())
                .secretKey(savedJob.getSecretKey())
                .fileName(savedJob.getFileName())
                .jobStatus(savedJob.getJobStatus())
                .conversionStatus(savedJob.getConversionStatus())
                .totalPages(savedJob.getTotalPages())
                .expiresAt(savedJob.getExpiresAt())
                .createdAt(savedJob.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public PrintResponse getFileByPin(String secretKey, UUID shopId) {
        log.debug("Fetching file for PIN: {}", secretKey);

        PrintJob printJob = printJobRepository
                .findBySecretKeyAndJobStatus(secretKey, JobStatus.PENDING)
                .orElseThrow(() -> new InvalidPinException("Invalid or expired PIN: " + secretKey));

        if (printJob.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidPinException("PIN has expired. Please upload again.");
        }

        ShopVerificationResponse shop = userServiceClient.verifyShop(shopId);
        if (!shop.isVerified() || !shop.isActive()) {
            throw new ShopNotVerifiedException("Shop is not verified or inactive");
        }

        printJob.setShopId(shop.getShopId());
        printJob.setJobStatus(JobStatus.PROCESSING);
        printJobRepository.save(printJob);

        // Dono files ke presigned URLs banayein
        String originalFileUrl = storageService.getPresignedUrl(printJob.getFileUrl());
        String convertedPdfUrl = printJob.getConvertedPdfUrl() != null 
                ? storageService.getPresignedUrl(printJob.getConvertedPdfUrl()) 
                : null;

        return PrintResponse.builder()
                .fileName(printJob.getFileName())
                .fileUrl(originalFileUrl)       // Download button ke liye
                .convertedPdfUrl(convertedPdfUrl) // Instant Print button ke liye 
                .totalPages(printJob.getTotalPages()) // Pages
                .message("File ready for printing")
                .build();
    }

    @Override
    @Transactional
    public PrintResponse printJob(String secretKey, UUID userId) {
        log.info("Incoming print request - PIN: {}, Request User ID: {}", secretKey, userId);

        PrintJob printJob = printJobRepository
                .findBySecretKeyAndJobStatus(secretKey, JobStatus.PROCESSING)
                .orElseThrow(() -> new InvalidPinException("Job not in PROCESSING state"));

        ShopVerificationResponse shop = userServiceClient.verifyShop(userId);
        UUID actualShopId = shop.getShopId(); 

        log.info("Verification Check -> DB Stored ShopId: {}, Actual ShopId: {}", printJob.getShopId(), actualShopId);

        if (printJob.getShopId() == null || !printJob.getShopId().equals(actualShopId)) {
            log.warn("Security Alert: Unauthorized print attempt for PIN: {}", secretKey);
            throw new ShopNotVerifiedException(
                    String.format("Unauthorized: Job shopId=%s, Request shopId=%s",
                            printJob.getShopId(), actualShopId)
            );
        }

        printJob.setJobStatus(JobStatus.READY);
        printJobRepository.save(printJob);

        log.info("Print Job {} status changed to READY by Shop {}", printJob.getId(), actualShopId);

        return PrintResponse.builder()
                .fileName(printJob.getFileName())
                .message("Print completed successfully! Ready for pickup.")
                .build();
    }

    @Override
    public JobResponse getJobStatus(String pin) {
        PrintJob job = printJobRepository.findBySecretKey(pin)
                .orElseThrow(() -> new InvalidPinException("Invalid PIN: " + pin));

        return JobResponse.builder()
                .id(job.getId())
                .secretKey(job.getSecretKey())
                .fileName(job.getFileName())
                .jobStatus(job.getJobStatus())
                .conversionStatus(job.getConversionStatus())
                .totalPages(job.getTotalPages())
                .expiresAt(job.getExpiresAt())
                .createdAt(job.getCreatedAt())
                .build();
    }


}

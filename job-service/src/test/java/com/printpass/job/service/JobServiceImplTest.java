package com.printpass.job.service;

import com.printpass.job.dto.response.JobResponse;
import com.printpass.job.dto.response.PrintResponse;
import com.printpass.job.dto.response.ShopVerificationResponse;
import com.printpass.job.entity.PrintJob;
import com.printpass.job.enums.JobStatus;
import com.printpass.job.exception.InvalidPinException;
import com.printpass.job.exception.ShopNotVerifiedException;
import com.printpass.job.feign.UserServiceClient;
import com.printpass.job.repository.PrintJobRepository;
import com.printpass.job.service.impl.JobServiceImpl;
import com.printpass.job.storage.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobServiceImplTest {

    @Mock
    private PrintJobRepository printJobRepository;

    @Mock
    private StorageService storageService;

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private JobServiceImpl jobService;

    private PrintJob mockJob;
    private UUID shopId;
    private UUID jobId;

    @BeforeEach
    void setUp() {
        shopId = UUID.randomUUID();
        jobId = UUID.randomUUID();

        mockJob = PrintJob.builder()
                .id(jobId)
                .secretKey("123456")
                .fileName("test.pdf")
                .fileUrl("http://minio/test.pdf")
                .jobStatus(JobStatus.PENDING)
                .shopId(shopId)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();
    }

    // ==========================================
    // TEST 1: File Upload
    // ==========================================
    @Test
    void uploadFile_ShouldSaveJobAndReturnPin() {
        // Arrange
        MultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "dummy content".getBytes());
        
        when(storageService.uploadFile(any(MultipartFile.class))).thenReturn("http://minio/test.pdf");
        when(printJobRepository.existsBySecretKey(anyString())).thenReturn(false); // PIN unique hai
        when(printJobRepository.save(any(PrintJob.class))).thenReturn(mockJob);

        // Act
        JobResponse response = jobService.uploadFile(file);

        // Assert
        assertNotNull(response);
        assertEquals("123456", response.getSecretKey());
        assertEquals("test.pdf", response.getFileName());
        verify(storageService, times(1)).uploadFile(file);
        verify(printJobRepository, times(1)).save(any(PrintJob.class));
    }

    // ==========================================
    // TEST 2: Get File By PIN (Success)
    // ==========================================
    @Test
    void getFileByPin_ShouldChangeStatusToProcessing_WhenValid() {
        // Arrange
        when(printJobRepository.findBySecretKeyAndJobStatus("123456", JobStatus.PENDING)).thenReturn(Optional.of(mockJob));
        
        ShopVerificationResponse verifiedShop = ShopVerificationResponse.builder()
                .shopId(shopId).isVerified(true).isActive(true).build();
        when(userServiceClient.verifyShop(shopId)).thenReturn(verifiedShop);
        
        when(storageService.getPresignedUrl(anyString())).thenReturn("http://minio/presigned.pdf");

        // Act
        PrintResponse response = jobService.getFileByPin("123456", shopId);

        // Assert
        assertNotNull(response);
        assertEquals(JobStatus.PROCESSING, mockJob.getJobStatus()); // Status change verify karein
        verify(printJobRepository, times(1)).save(mockJob);
    }

    // ==========================================
    // TEST 3: Get File By PIN (Invalid PIN)
    // ==========================================
    @Test
    void getFileByPin_ShouldThrowException_WhenPinNotFound() {
        // Arrange
        when(printJobRepository.findBySecretKeyAndJobStatus("123456", JobStatus.PENDING)).thenReturn(Optional.empty());

        // Act & Assert
        InvalidPinException exception = assertThrows(InvalidPinException.class, () -> {
            jobService.getFileByPin("123456", shopId);
        });
        assertTrue(exception.getMessage().contains("Invalid or expired PIN"));
    }

    // ==========================================
    // TEST 4: Print Job (Success)
    // ==========================================
    @Test
    void printJob_ShouldChangeStatusToReady_WhenShopMatches() {
        // Arrange
        mockJob.setJobStatus(JobStatus.PROCESSING);
        when(printJobRepository.findBySecretKeyAndJobStatus("123456", JobStatus.PROCESSING)).thenReturn(Optional.of(mockJob));

        ShopVerificationResponse shopResponse = ShopVerificationResponse.builder()
                .shopId(shopId).build();
        when(userServiceClient.verifyShop(shopId)).thenReturn(shopResponse);

        // Act
        PrintResponse response = jobService.printJob("123456", shopId);

        // Assert
        assertNotNull(response);
        assertEquals(JobStatus.READY, mockJob.getJobStatus());
        verify(printJobRepository, times(1)).save(mockJob);
    }

    // ==========================================
    // TEST 5: Print Job (IDOR Protection Fail)
    // ==========================================
    @Test
    void printJob_ShouldThrowException_WhenShopDoesNotMatch() {
        // Arrange
        mockJob.setJobStatus(JobStatus.PROCESSING);
        when(printJobRepository.findBySecretKeyAndJobStatus("123456", JobStatus.PROCESSING)).thenReturn(Optional.of(mockJob));

        // Fake shop ID return karein jo match nahi karta
        UUID hackerShopId = UUID.randomUUID();
        ShopVerificationResponse hackerResponse = ShopVerificationResponse.builder()
                .shopId(hackerShopId).build();
        when(userServiceClient.verifyShop(shopId)).thenReturn(hackerResponse);

        // Act & Assert
        assertThrows(ShopNotVerifiedException.class, () -> {
            jobService.printJob("123456", shopId);
        });
        verify(printJobRepository, never()).save(mockJob); // Save nahi hona chahiye
    }

    // ==========================================
    // TEST 6: Reprint Job
    // ==========================================
    @Test
    void reprintJob_ShouldGenerateNewPin() {
        // Arrange
        when(printJobRepository.findBySecretKey("123456")).thenReturn(Optional.of(mockJob));
        when(printJobRepository.existsBySecretKey(anyString())).thenReturn(false);
        when(printJobRepository.save(any(PrintJob.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        JobResponse response = jobService.reprintJob("123456");

        // Assert
        assertNotNull(response);
        assertNotEquals("123456", response.getSecretKey()); // Naya PIN hona chahiye
        verify(printJobRepository, times(1)).save(any(PrintJob.class));
    }
}
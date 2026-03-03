package com.printpass.job.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import com.printpass.job.client.ShopClient;
import com.printpass.job.client.UserClient;
import com.printpass.job.dto.JobRequest;
import com.printpass.job.dto.JobResponse;
import com.printpass.job.dto.ShopDto;
import com.printpass.job.dto.UserDto;
import com.printpass.job.entity.PrintJob;
import com.printpass.job.enums.JobStatus;
import com.printpass.job.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final UserClient userClient;
    private final ShopClient shopClient;
    @Autowired
    private CloudStorageService cloudStorageService;
    // Folder kahan banega? Aapke project ke andar 'uploads' ke naam se
    private final String UPLOAD_DIR = "uploads/";


    public JobResponse createJob(JobRequest request, MultipartFile file) {

        // 1. Validation (Student & Shop ki wahi purani logic)
        UserDto student;
        try {
            student = userClient.getUserById(request.getStudentId());
        } catch (Exception e) {
            throw new RuntimeException("Error: Student ID ghalat hai ya mojood nahi!");
        }
        if (!"STUDENT".equals(student.getRole())) {
            throw new RuntimeException("Error: Sirf ek STUDENT hi print order de sakta hai!");
        }

        ShopDto shop;
        try {
            shop = shopClient.getShopById(request.getShopId());
        } catch (Exception e) {
            throw new RuntimeException("Error: Shop ID ghalat hai ya mojood nahi!");
        }
        if (!shop.isOpen()) {
            throw new RuntimeException("Error: Maazrat, yeh dukan abhi band hai!");
        }

        // 2. CLOUD UPLOAD LOGIC (Naya Aasmaani Jadu! ☁️🚀)
        String cloudFileUrl;
        try {
            // File seedha DigitalOcean Spaces par jayegi aur uska pakka URL wapas aayega!
            cloudFileUrl = cloudStorageService.uploadFileToCloud(file);
        } catch (IOException e) {
            throw new RuntimeException("Error: File cloud par upload nahi ho saki!", e);
        }

        // 3. Bill Calculation
        BigDecimal cost = shop.getPerPageCost().multiply(BigDecimal.valueOf(request.getTotalPages()));
        if (request.isColor()) {
            cost = cost.multiply(BigDecimal.valueOf(3));
        }

        // 4. Secret PIN Generate karein
        String secretPin = String.format("%06d", new java.util.Random().nextInt(1000000));

        // 5. Order (Job) Save karein
        PrintJob job = PrintJob.builder()
                .studentId(request.getStudentId())
                .shopId(request.getShopId())
                // Ab yahan local naam ke bajaye Cloud ka poora URL save hoga!
                .documentName(cloudFileUrl)
                .totalPages(request.getTotalPages())
                .isColor(request.isColor())
                .totalCost(cost)
                .status(JobStatus.PENDING)
                .pin(secretPin)
                .build();

        PrintJob savedJob = jobRepository.save(job);
        return mapToResponse(savedJob);
    }




    public List<JobResponse> getAllJobs() {
        return jobRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    // 🔄 Dukan wale ke liye: Job ka status update karne ki API
    public JobResponse updateJobStatus(Long jobId, JobStatus newStatus) {

        // 1. Database se order (Job) dhoondna
        PrintJob job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Error: Yeh Job ID mojood nahi hai!"));

        // 2. Naya status set karna (Misaal: PENDING se COMPLETED)
        job.setStatus(newStatus);

        // 3. 🎯 THE PRO LEVEL MAGIC! (Auto-Delete Logic)
        // Agar naya status 'COMPLETED' hai, aur file ka URL abhi bhi mojood hai...
        if (newStatus == JobStatus.COMPLETED && job.getDocumentName() != null && job.getDocumentName().startsWith("http")) {

            // Cloud se file ko foran delete kardo!
            cloudStorageService.deleteFileFromCloud(job.getDocumentName());

            // Database mein bhi URL ki jagah ek message likh do taake dead link na bache
            job.setDocumentName("DELETED_SECURELY_AFTER_PRINT");
        }

        // 4. Update ki hui Job ko wapas database mein save karna
        PrintJob updatedJob = jobRepository.save(job);
        return mapToResponse(updatedJob);
    }

    // mapToResponse mein bhi update karein
    private JobResponse mapToResponse(PrintJob job) {
        return JobResponse.builder()
                .id(job.getId())
                .studentId(job.getStudentId())
                .shopId(job.getShopId())
                .documentName(job.getDocumentName())
                .totalPages(job.getTotalPages())
                .totalCost(job.getTotalCost())
                .status(job.getStatus())
                .pin(job.getPin()) // Nayi Line: Student ko PIN wapas bhejein
                .build();
    }


    // Naya Method: Vendor ke liye PIN verify karna
    public JobResponse getJobByPin(String pin) {
        PrintJob job = jobRepository.findByPin(pin)
                .orElseThrow(() -> new RuntimeException("Error: This pin is wrong or order is not present!"));

        return mapToResponse(job);
    }
}
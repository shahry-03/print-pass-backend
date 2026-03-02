package com.printpass.job.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
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
import java.util.Random;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final UserClient userClient;
    private final ShopClient shopClient;
    // Folder kahan banega? Aapke project ke andar 'uploads' ke naam se
    private final String UPLOAD_DIR = "uploads/";


    public JobResponse createJob(JobRequest request, MultipartFile file) { // File argument add kiya

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

        // 2. FILE SAVING LOGIC (Naya Jadu!)
        File directory = new File(UPLOAD_DIR);
        if (!directory.exists()) {
            directory.mkdirs(); // Agar uploads folder nahi hai, toh bana do
        }

        // File ka original naam lo (e.g., "notes.pdf") aur uske aage ek random ID (UUID) laga do
        // Taake agar 2 students "notes.pdf" bhejein, toh purani file delete na ho jaye
        String originalFilename = file.getOriginalFilename();
        String uniqueFilename = UUID.randomUUID().toString() + "_" + originalFilename;
        Path filePath = Paths.get(UPLOAD_DIR + uniqueFilename);

        try {
            // File ko copy karke uploads folder mein rakh do
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Error: File server par save nahi ho saki!", e);
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
                .documentName(uniqueFilename) // Database mein naya UNIQUE naam save karein
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

    // Naya Method: Job ka Status Update karna
    public JobResponse updateJobStatus(Long jobId, JobStatus newStatus) {
        // 1. Pehle database se purana order dhoondein
        PrintJob job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Error: Yeh Print Job ID database mein nahi mili!"));

        // 2. Uska status update karein
        job.setStatus(newStatus);

        // 3. Database mein wapas save kar dein
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
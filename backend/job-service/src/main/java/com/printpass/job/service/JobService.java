package com.printpass.job.service;

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

    public JobResponse createJob(JobRequest request) {

        // 1. Check karein kya yeh waqai koi STUDENT hai?
        UserDto student;
        try {
            student = userClient.getUserById(request.getStudentId());
        } catch (Exception e) {
            throw new RuntimeException("Error: Student ID ghalat hai ya mojood nahi!");
        }
        if (!"STUDENT".equals(student.getRole())) {
            throw new RuntimeException("Error: Sirf ek STUDENT hi print order de sakta hai!");
        }

        // 2. Check karein kya DUKAN mojood hai aur khuli hai?
        ShopDto shop;
        try {
            shop = shopClient.getShopById(request.getShopId());
        } catch (Exception e) {
            throw new RuntimeException("Error: Shop ID ghalat hai ya mojood nahi!");
        }
        if (!shop.isOpen()) {
            throw new RuntimeException("Error: Maazrat, yeh dukan abhi band hai!");
        }

        // 3. Bill Calculation (The Brain)
        // Basic Cost = total pages * shop ki per page cost
        BigDecimal cost = shop.getPerPageCost().multiply(BigDecimal.valueOf(request.getTotalPages()));

        // Agar color print hai, toh hum cost ko 3 se multiply (triple) kar dete hain
        if (request.isColor()) {
            cost = cost.multiply(BigDecimal.valueOf(3));
        }

        // 4. Order (Job) Save karein
        PrintJob job = PrintJob.builder()
                .studentId(request.getStudentId())
                .shopId(request.getShopId())
                .documentName(request.getDocumentName())
                .totalPages(request.getTotalPages())
                .isColor(request.isColor())
                .totalCost(cost)
                .status(JobStatus.PENDING) // Order abhi PENDING hai
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

    private JobResponse mapToResponse(PrintJob job) {
        return JobResponse.builder()
                .id(job.getId())
                .studentId(job.getStudentId())
                .shopId(job.getShopId())
                .documentName(job.getDocumentName())
                .totalPages(job.getTotalPages())
                .totalCost(job.getTotalCost())
                .status(job.getStatus())
                .build();
    }
}
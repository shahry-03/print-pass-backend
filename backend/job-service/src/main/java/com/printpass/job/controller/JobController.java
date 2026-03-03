package com.printpass.job.controller;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.printpass.job.dto.JobRequest;
import com.printpass.job.dto.JobResponse;
import com.printpass.job.enums.JobStatus;
import com.printpass.job.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    // NAYA: File aur data ek sath lene ke liye
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public JobResponse createJob(
            @RequestParam("studentId") Long studentId,
            @RequestParam("shopId") Long shopId,
            @RequestParam("totalPages") Integer totalPages,
            @RequestParam("isColor") boolean isColor,
            @RequestParam("file") MultipartFile file) { // Nayi cheez: Asli File!

        // DTO khud banayenge
        JobRequest request = new JobRequest();
        request.setStudentId(studentId);
        request.setShopId(shopId);
        request.setTotalPages(totalPages);
        request.setColor(isColor);
        // documentName ab file se aayega, isliye usay yahan nahi bhej rahe

        return jobService.createJob(request, file); // Service ko file bhi pass kardi
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<JobResponse> getAllJobs() {
        return jobService.getAllJobs();
    }



    // Nayi GET API: Vendor isay hit karega
    @GetMapping("/verify/{pin}")
    @ResponseStatus(HttpStatus.OK)
    public JobResponse verifyPin(@PathVariable String pin) {
        return jobService.getJobByPin(pin);
    }

    // Naya Darwaza: Asli file download karne ke liye
    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        try {
            // Uploads folder ka rasta
            Path filePath = Paths.get("uploads/").resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            // Agar file mojood hai aur parhi ja sakti hai
            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        // CONTENT_DISPOSITION browser ko batata hai ke is file ko download karna hai
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                throw new RuntimeException("Error: File nahi mili ya delete ho chuki hai!");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error: File download karne mein masla aaya!", e);
        }
    }

    // 🔄 Status Update API (Yeh API file delete ka jadu bhi chalayegi)
    @PutMapping("/{jobId}/status")
    public ResponseEntity<JobResponse> updateStatus(
            @PathVariable Long jobId,
            @RequestParam("newStatus") JobStatus newStatus) {

        JobResponse response = jobService.updateJobStatus(jobId, newStatus);
        return ResponseEntity.ok(response);
    }
}